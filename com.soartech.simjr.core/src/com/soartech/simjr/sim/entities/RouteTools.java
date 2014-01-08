package com.soartech.simjr.sim.entities;

import java.util.List;

import com.soartech.simjr.ProgressMonitor;
import com.soartech.simjr.SimulationException;
import com.soartech.simjr.scenario.EntityElement;
import com.soartech.simjr.scenario.PointElementList;
import com.soartech.simjr.scenario.model.Model;
import com.soartech.simjr.services.ServiceManager;
import com.soartech.simjr.services.SimulationService;

public class RouteTools implements SimulationService
{
    com.soartech.simjr.scenario.model.Model model;
    
    public RouteTools(Model model)
    {
        this.model = model;
    }
    
    public RouteTools(ServiceManager services, Model model) 
    {
        this(model);
    }
    
    public Direction getRouteDirection(String RouteName)
    {
        if(RouteName == null)
            return Direction.NORTH;
        //Determine the Directionality of the Route
        EntityElement route = model.getEntities().getEntity(RouteName);
        return getRouteDirection(route);
    }

    public Direction getRouteDirection(EntityElement route)
    {
        PointElementList pointList = route.getPoints();
        
        List<String> points = pointList.getPoints();
        
        return getRouteDirection(points);
    }
    
    public Direction getRouteDirection(List<String> points)
    {

        
        String startPointName = points.get(0);
        String endPointName = points.get(points.size()-1);
        
        //Find a vector between the two points
        EntityElement startPoint = model.getEntities().getEntity(startPointName);
        EntityElement endPoint = model.getEntities().getEntity(endPointName);
        //Determine the General Direction
        double latDiff = endPoint.getLocation().getLatitude() - startPoint.getLocation().getLatitude();
        double longDiff = endPoint.getLocation().getLongitude() - startPoint.getLocation().getLongitude();
        
        double theta = Math.atan(latDiff/longDiff);
        
        if(theta > Math.PI/4)
        {
            return Direction.NORTH;
        }
        else if (theta <= Math.PI/4 && theta >= -1.0 * Math.PI/4)
        {
            return Direction.EAST;
        }
        else if(theta < -1.0 * Math.PI/4)
        {
            return Direction.SOUTH;
        }
        else
        {
            return Direction.WEST;
        }
        
        
    }
    
    
//    public List<String> getReversedRoute(String name)
//    {
//        List<String> points = routePoints.get(name);
//        List<String> reversed = new ArrayList<String>();
//        for(int i=points.size()-1; i>=0; i--)
//        {
//            reversed.add(points.get(i));
//        }
//        return reversed;
//    }
    @Override
    public Object getAdapter(Class<?> klass)
    {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public void start(ProgressMonitor progress) throws SimulationException
    {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void shutdown() throws SimulationException
    {
        // TODO Auto-generated method stub
        
    }
}
