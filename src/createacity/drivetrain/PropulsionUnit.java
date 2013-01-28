/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package createacity.drivetrain;
import com.jme3.bullet.control.VehicleControl;

/**
 * A propulsionunit controllable by a PropulsionController. Contains and controls engine,gearbox and drivetrain.
 * @author Vekjeft
 */
public abstract class PropulsionUnit{
    PropulsionController propulsionController;
    VehicleControl vehicleControl;
    Shifter shifter;
    float throttleValue = 0;
    boolean accelerate = false;
    boolean isReversing = false;    
 
    public PropulsionUnit(VehicleControl vehicleControl){
    this.vehicleControl = vehicleControl;
    }
    
    public void activate(float value, boolean accelerate){  // Altered by PropulsionController
    }
    public void update(float tpf){                          // Updated from physicsTickListener
    }
    public void gearUp(){ 
    }
    public void gearDown(){
    }
    
    public Shifter getShifter() {
        return shifter;
    }
    
}
