package com.soartech.simjr.ui.editor.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.soartech.math.Vector3;
import com.soartech.math.geotrans.Geocentric;
import com.soartech.math.geotrans.Geodetic;
import com.soartech.simjr.scenario.edits.NewEntityEdit;
import com.soartech.simjr.ui.actions.ActionManager;
import com.soartech.simjr.ui.pvd.PlanViewDisplayProvider;

public class ImportOSMAction extends AbstractEditorAction 
{

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ImportOSMAction.class);

    private Map<String,Node> idToNodeMap = new HashMap<String,Node>();
    
    // TODO: I need some additional temporary storage for the node information
    // because I have to figure out an approximate centroid for the area before
    // I add the waypoints and routes to the map. It seems like the map should
    // respond appropriately to the change after the routes and waypoints are added
    // but it doesn't seem to.
    private static class Node {
        String id;
        String name;
        double latitude;
        double longitude;
    }
    
    /**
     * Creates a new open street map data import action and registers it
     * with the action manager.
     * 
     * @param actionManager the action manager to register with
     */
    public ImportOSMAction(ActionManager actionManager) 
    {
        super( actionManager, "Import Open Street Map Data...");
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent arg0) 
    {
        String fname = "";
        try
        {
            final File selectedFile = getApplication().selectFile("osm", "Open Street Map XML Data");
            fname = ""+selectedFile;
            if(selectedFile == null)
            {
                return;
            }

            importOSMDataFromFile( selectedFile );
            refreshDisplay();
        }
        catch (Exception e1)
        {
            String errMsg = "Error loading open street map data file "+fname;
            getApplication().showError(errMsg, e1);
            logger.error(errMsg, e1);
        }

    }
    
    private void importOSMDataFromFile(File file) throws JDOMException, IOException
    {
        SAXBuilder sb = new SAXBuilder();
        Document doc = sb.build(file);
        
        importOSMDataFromDoc(doc);
    }
    
    private void importOSMDataFromDoc(Document doc) 
    {
        readInNodeData(doc);
        
        // The terrain origin must be set before entities are added to the terrain (seems like a bug
        // since the model document has the right data in it anyway)
        if ( !getModel().getTerrain().getImage().hasImage() )
        {
            Geodetic.Point gdorigin = calculateNodeCenter();
            getModel().getTerrain().setOrigin(Math.toDegrees(gdorigin.latitude), Math.toDegrees(gdorigin.longitude));
        }
        
        // Creating the various entities (you need the id to name map because of possible
        // name collisions). A waypoint's id might not match the name assigned to it on creation.
        createRouteEntities(doc);        
    }

    private void createRouteEntities(Document doc) {
        // Creating the route entities
        List<?> ways = doc.getRootElement().getChildren("way");
        for ( Object obj : ways ) 
        {
            Element wayElem = (Element) obj;
            String id = wayElem.getAttributeValue("id");
            Map<String,String> tags = processTagInfo(wayElem);

            // Making sure we just use highways that aren't areas (for some reason
            // rest areas are encoded as "ways" with an area attribute
            String highwayType = tags.get("highway");
            String area = tags.get("area");
            if ( highwayType != null && area == null)
            {
                String name = tags.get("name");
                if ( name == null ) name = id;
                
                List<?> nodeRefElems = wayElem.getChildren("nd");
                List<String> nodeNames = new ArrayList<String>();
                for ( Object ooo : nodeRefElems ) 
                {
                    Element nref = (Element) ooo;
                    String nid = nref.getAttributeValue("ref");
                    
                    Node node = getOrCreateNode(nid);
                    nodeNames.add(node.name);
                }
                
                NewEntityEdit edit = getModel().getEntities().addEntity(name, "route");
                edit.getEntity().getPoints().setPoints(nodeNames);
                edit.getEntity().setLabelVisible(false);
            }
        }
    }
    
    /**
     * This returns the node with the specified id. If the name hasn't been initialized
     * we create a waypoint entity and store its name.
     * 
     * @param nodeid
     * @return Node with id of {@code nodeid}.
     */
    private Node getOrCreateNode(String nodeid)
    {
        Node node = idToNodeMap.get(nodeid);
        
        // If there is nothing in the name field we need to create a waypoint entity
        if ( node.name == null )
        {
            NewEntityEdit edit = getModel().getEntities().addEntity(node.id, "waypoint");
            
            // TODO: Not sure where to get altitude of ground level for the following
            edit.getEntity().getLocation().setLocation(node.latitude, node.longitude, 0.);
            edit.getEntity().setVisible(false);
            edit.getEntity().setLabelVisible(false);
            
            node.name = edit.getEntity().getName();            
        }
        return node;
    }

    private Geodetic.Point calculateNodeCenter() {
        // These variables are used to estimate a good terrain origin based on
        // the lat/lon of the input road points
        Geocentric geocentric = new Geocentric();
        Vector3 sum = new Vector3(0.,0.,0.);
        long nnodes = 0;
        
        for ( Node node : idToNodeMap.values() )
        {   
            // Accumulating position info for later origin calculation
            Vector3 gcpos = geocentric.fromGeodetic(Math.toRadians(node.latitude), 
                                                    Math.toRadians(node.longitude), 
                                                    0.);
            sum = sum.add(gcpos);
            nnodes++;
        }
        
        // Calculating and setting a good origin value (has to be done before anything is added
        // to the scenario)
        Vector3 origin = sum.multiply(1./((double) nnodes));
        Geodetic.Point gdorigin = geocentric.toGeodetic(origin.x, origin.y, origin.z);
        return gdorigin;
    }

    private void readInNodeData(Document doc) {
        // Creating the waypoint entities
        List<?> nodeElems = doc.getRootElement().getChildren("node");
        
        for ( Object obj : nodeElems ) 
        {
            Node node = new Node();
            Element nodeElem = (Element) obj;
            node.id = nodeElem.getAttributeValue("id");
            node.latitude = Double.parseDouble( nodeElem.getAttributeValue("lat") );
            node.longitude = Double.parseDouble( nodeElem.getAttributeValue("lon") );
            idToNodeMap.put(node.id, node);
        }
    }
    
    private Map<String,String> processTagInfo(Element waypointElem) 
    {
        Map<String,String> retval = new HashMap<String,String>();
        List<?> tags = waypointElem.getChildren("tag");
        for ( Object obj : tags ) 
        {
            Element tagElem = (Element) obj;
            String key = tagElem.getAttributeValue("k");
            String value = tagElem.getAttributeValue("v");
            retval.put(key, value);
        }
        return retval;
    }
    
    private void refreshDisplay()
    {
        final PlanViewDisplayProvider pvdPro = findService(PlanViewDisplayProvider.class);
        if(pvdPro != null && pvdPro.getActivePlanViewDisplay() != null)
        {
            pvdPro.getActivePlanViewDisplay().showAll();
        }
    }

    /* (non-Javadoc)
     * @see com.soartech.simjr.ui.actions.AbstractSimulationAction#update()
     */
    @Override
    public void update() 
    {
        // Nothing to do here. Typically used to update the state of the action
        // (enable or disable based on application state) when application state
        // changes.
    }
}
