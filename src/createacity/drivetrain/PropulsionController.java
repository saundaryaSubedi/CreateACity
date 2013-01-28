/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package createacity.drivetrain;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import java.io.IOException;
import java.util.ArrayList;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.scene.control.Control;


/**
 * A controller for a series of propulsionunits
 * @author Vekjeft
 */
public class PropulsionController implements PhysicsTickListener,ActionListener,AnalogListener,Control{
    public static String accForward = "AccelerateForward";
    public static String accBackward = "AccelerateBackward";
    public static String gearUp = "GearUp";
    public static String gearDown = "GearDown";
    
    private float throttle;
    private float lastThrottle;
    private boolean isPressedFarward;
    private boolean isPressedBackward;

    private boolean resetInput;         // For synching physics updates and input AnalogListener.
    
    private String name;    

    ArrayList<PropulsionUnit> propulsionUnits;
    
    public PropulsionController(ArrayList<PropulsionUnit> propulsionUnits,String name){
    this.name = name;
    this.propulsionUnits = propulsionUnits;

    }

    public String getUniqueActionName(String action){
    return name+action;
    }    
    public String getName(){
    return name;
    }

    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals(this.name+gearUp) && !isPressed){
            for(PropulsionUnit x:propulsionUnits){
                x.getShifter().gearUp();
                
            }
        }
        
        if (name.equals(this.name+gearDown) && !isPressed){
            for(PropulsionUnit x:propulsionUnits){
                x.getShifter().gearDown();
            }
        }
    }
    
    public void onAnalog(String action, float value, float tpf){
        if(action.equals(name+accForward))
        {
        this.isPressedFarward = true;
        
        }
        else if(action.equals(name+accBackward)){
        this.isPressedBackward = true;
        }        
    }
    

    public void prePhysicsTick(PhysicsSpace space,float tpf){
            if(isPressedFarward)
            {
                if(throttle+tpf*2<1)
                {
                throttle +=tpf*2;
                }
                else{
                throttle = 1;
                }
                
            resetInput = true;
            }
            else if(isPressedBackward){
                if(throttle-tpf*2>-1)
                {
                throttle -=tpf*2;
                }
                else{
                throttle = -1;
                }
            resetInput = true;            
            }
            else
            {                 
                if(throttle-tpf*2>0)
                {
                throttle -=tpf*2;
                }
                else if(throttle+tpf*2<0){
                throttle +=tpf*2;
                }
                else{
                throttle = 0;
                }
            }
        for(PropulsionUnit x:propulsionUnits){
            if(lastThrottle != throttle)
            {
                if(throttle==0){
                x.activate(throttle, false);
                lastThrottle = throttle;
                }
                else{
                x.activate(throttle, true);
                lastThrottle = throttle;
                }
            }
        x.update(tpf);
        }     
        
    }
    public void physicsTick(PhysicsSpace space,float tpf){

    }

    
    
    public Control cloneForSpatial(Spatial sptl) {
    throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSpatial(Spatial sptl) {
    // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEnabled(boolean bln) {
    throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isEnabled() {
    throw new UnsupportedOperationException("Not supported yet.");
    }

    public void update(float f) {

    }

    public void render(RenderManager rm, ViewPort vp) {
        if(resetInput){
        this.isPressedFarward = false;
        this.isPressedBackward = false;
        resetInput = false;
        }        
    }

    public void write(JmeExporter je) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
    }

    public void read(JmeImporter ji) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
    }

    
}
