/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package createacity.drivetrain;

import java.util.ArrayList;
/**
 *
 * @author Vekjeft
 */
public class GearBox{
    ArrayList<Float> gearRatios;  // Gear ratio of each gear
    float gearShiftTime;  // Time required to change gear, method to delay gearshift must be implemented

    public int getNumberOfGears(){
    return gearRatios.size();
    }
    public GearBox(){
    gearRatios = new ArrayList<Float>();
    }
    public void addGearRatio(int gear,float ratio){
    gearRatios.add(gear, ratio);
    }
    public float getRatioFromGear(int gear){
    return gearRatios.get(gear);
    }
    public void setGearShiftTime(float gearShiftTime){
    this.gearShiftTime = gearShiftTime;
    }
    public float getGearShiftTime(){
    return gearShiftTime;
    }
    public ArrayList<Float> getGearRatios(){
    return gearRatios;
    }
}
