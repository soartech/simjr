package com.soartech.simjr.view3D;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.util.CameraUtility;

/**
* @author Tu Lam
*  
*/
public class SimNavigationTool extends ShipNavigationTool
{
    private transient InputSlot rotateActivation = InputSlot.getDevice("PrimaryAction");
//    private transient InputSlot rotateActivation = InputSlot.getDevice("ShipRotateActivation");
    private transient final InputSlot verticalRotation = InputSlot.getDevice("PointerNdcY");
    
    private double maxAngle = Math.PI*0.5;
    private double minAngle = -Math.PI*0.5;

    private boolean invert;

    private double lastPointerY = 0;
    private double prevVertRotAngle = -Math.PI*0.25;
    private double currVertRotAngle = -Math.PI*0.25;
    private transient boolean rotate;

    public SimNavigationTool() {
        addCurrentSlot(verticalRotation);
        addCurrentSlot(rotateActivation);
    }

    public void perform(ToolContext tc) {
        matrixLocked = true;
        super.perform(tc);
        matrixLocked = false;

        SceneGraphComponent myComponent = tc.getRootToToolComponent().getLastComponent();

        if (rotate) {
            if (!tc.getAxisState(rotateActivation).isPressed()) {
                removeCurrentSlot(verticalRotation);
                rotate = false;
            }
        } else {
            if (tc.getAxisState(rotateActivation).isPressed()) {
                addCurrentSlot(verticalRotation);
                rotate = true;
                lastPointerY = tc.getAxisState(verticalRotation).doubleValue();
            }

            if (tc.getSource() == rotateActivation || !rotate) {
                Matrix myMatrix = MatrixBuilder.euclidean().translate(currTranslation).rotateY(currHorizRotAngle).rotateX(currVertRotAngle).getMatrix();
                myMatrix.assignTo(myComponent);
                fireCameraChanged(tc);
                
                return;
            }
        }

        if (rotate && tc.getSource() == verticalRotation) {
            double currPointerY = tc.getAxisState(verticalRotation).doubleValue();
            double deltaAngle = (currPointerY - lastPointerY) * 2.5;

            if (currVertRotAngle + deltaAngle > maxAngle) {
                currVertRotAngle = maxAngle;
                Matrix myMatrix = MatrixBuilder.euclidean().translate(currTranslation).rotateY(currHorizRotAngle).rotateX(currVertRotAngle).getMatrix();
                myMatrix.assignTo(myComponent);
                fireCameraChanged(tc);
                
                return;
            }
            
            if (currVertRotAngle + deltaAngle < minAngle) {
                currVertRotAngle = minAngle;
                Matrix myMatrix = MatrixBuilder.euclidean().translate(currTranslation).rotateY(currHorizRotAngle).rotateX(currVertRotAngle).getMatrix();
                myMatrix.assignTo(myComponent);
                fireCameraChanged(tc);
                
                return;
            }
            
            currVertRotAngle += deltaAngle;

            lastPointerY = currPointerY;
        }
    
        Matrix myMatrix = MatrixBuilder.euclidean().translate(currTranslation).rotateY(currHorizRotAngle).rotateX(currVertRotAngle).getMatrix();
        myMatrix.assignTo(myComponent);
        fireCameraChanged(tc);
    }

    public double getMaxAngle() {
        return maxAngle;
    }

    public void setMaxAngle(double maxAngle) {
        this.maxAngle = maxAngle;
    }

    public double getMinAngle() {
        return minAngle;
    }

    public void setMinAngle(double minAngle) {
        this.minAngle = minAngle;
    }

    public boolean isInvert() {
        return invert;
    }

    public void setInvert(boolean invert) {
        this.invert = invert;
    }
    
    protected void fireCameraChanged(ToolContext tc) {
        if(prevHorizRotAngle != currHorizRotAngle
                || prevVertRotAngle != currVertRotAngle
                || currTranslation[0] != prevTranslation[0]
                || currTranslation[1] != prevTranslation[1]
                || currTranslation[2] != prevTranslation[2]
                || currTranslation[3] != prevTranslation[3]
                ) {
        
            // Quick way to call Camera's fireCameraChanged
            Camera camera = CameraUtility.getCamera(tc.getViewer());
            camera.setNear(camera.getNear());
            
            prevHorizRotAngle = currHorizRotAngle;
            prevVertRotAngle = currVertRotAngle;
            System.arraycopy(currTranslation, 0, prevTranslation, 0, 4);
        }
    }
}
