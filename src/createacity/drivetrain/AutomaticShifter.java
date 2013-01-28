/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package createacity.drivetrain;

/**
 *
 * @author Joseph
 */
public class AutomaticShifter implements Shifter {
    
    int value;
    int maxValue;
    int minValue;
    String[] gears;
    
    public AutomaticShifter(String[] gears){
        minValue = 0;
        maxValue = gears.length - 1;
        value = minValue;
        this.gears = gears;
    }

    public void gearUp() {
        if (value - 1 >= minValue)
            value--;
    }

    public void gearDown() {
        if (value + 1 <= maxValue)
            value++;
    }

    public void setGear(String newGear) {
        for(int i = 0; i < gears.length; i++){
            if (newGear.toUpperCase().equals(gears[i]))
                value = i;
        }
    }
    
    public int getGearValue(){
        return value;
    }

    public String getGear() {
        return gears[value];
    }
    
}
