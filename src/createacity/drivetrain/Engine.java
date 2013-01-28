/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package createacity.drivetrain;

import java.util.ArrayList;
import com.jme3.math.FastMath;
/**
 *
 * @author Vekjeft
 */
public class Engine{
    ArrayList<Float> torqueAtRpm;  // Powerprofile at each 1000 rpm, power is interpolated when RPM is in between 1000nds
    private int idleRpm;
    private int startRpm;
    private int maxRpm;
    
    private float shiftUpRpsLimit;
    private float shiftDownRpsLimit;
    
    public Engine(){
    this.torqueAtRpm = new ArrayList<Float>();
    }
    public void setStartRpm(int startRpm){
    this.startRpm = startRpm;
    }
    public int getStartRpm(){
    return startRpm;
    }
    public int getStartRps(){
    return startRpm/60;
    }            
    public void setMaxRpm(int maxRpm){
    this.maxRpm = maxRpm;
    }
    public int getMaxRpm(){
    return maxRpm;
    }
    public int getMaxRps(){
    return maxRpm/60;
    }
    public void setIdleRpm(int idleRpm){
    this.idleRpm = idleRpm;
    }    
    public float getIdleRpm(){
    return idleRpm;
    }
    public float getIdleRps(){
    return idleRpm/60;
    }    
    
    public void setShiftDownRpmLimit(int shiftDownRpmLimit){
    this.shiftDownRpsLimit = ((float) shiftDownRpmLimit)/60;
    }
    public float getShiftDownRpsLimit(){
    return shiftDownRpsLimit;
    }
    
    public void setShiftUpRpmLimit(int shiftUpRpmLimit){
    this.shiftUpRpsLimit = ((float) shiftUpRpmLimit)/60;
    }
    public float getShiftUpRpsLimit(){
    return shiftUpRpsLimit;
    }
    public void addTorqueInfo(int kRpm,float torque){
    torqueAtRpm.add(kRpm,torque);
    }
    public ArrayList<Float> getTorqueArray(){
    return torqueAtRpm;
    }
    
    public float getTorqueAtRPS(float rps,boolean accelerate){
    return getTorqueAtRPM((int) rps*60,accelerate);
    }
    public float getTorqueAtRPM(float rpm,boolean accelerate){

        if(accelerate)
        {
            int lowerRPM = ((Float) FastMath.floor((float) rpm/1000)).intValue();
            int upperRPM = ((Float) FastMath.ceil((float) rpm/1000)).intValue();

            if(upperRPM<=((Float) FastMath.ceil((float) maxRpm/1000)).intValue())
            {
            if(rpm<getStartRpm())
            {
            rpm = getStartRpm();
            }


            float kRpmToLower = ((float) (rpm-lowerRPM*1000))/1000;
            float lowerRPMTorque;
            
                if(lowerRPM>0){
                lowerRPMTorque = torqueAtRpm.get(lowerRPM);
                }
                else{
                lowerRPMTorque = 0;
                }
            
            float higherRPMTorque = torqueAtRpm.get(upperRPM);
            return FastMath.interpolateLinear(kRpmToLower, lowerRPMTorque, higherRPMTorque);
            }
            else{
            return 0;
            }
        }
        else
        {
            // Only apply breaktorque if engine rpm > idle rpm, and apply it gradually.
            // Engine rpm increase = engine breaktorque increase
            if(rpm>idleRpm)
            {
            return torqueAtRpm.get(0)*(rpm/((float) maxRpm));   // Breaktorque
            }
            else{
            return 0;   // Breaktorque
            }
        }

    }    
}
