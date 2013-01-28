/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package createacity.drivetrain;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

/**
 * Implementation of an internal combustion engine drivetrain.
 * @author Vekjeft
 */
public class InternalCombustionPropulsionUnit extends PropulsionUnit{
    private float engineRPM = 0;
    private float torque;

    private float speed;
    
    private int currentGear = 1;        // Gear 0 is reverse
    
    private Engine engine;
    private GearBox gearBox;
    private DriveTrain driveTrain;
    
    boolean hasBeenUsed = false;
    
    /**
     * 
     * @param engine
     * @param gearBox
     * @param driveTrain
     * @param vehicleControl Vehicle used
     */
    public InternalCombustionPropulsionUnit(Engine engine,GearBox gearBox,DriveTrain driveTrain,VehicleControl vehicleControl){
        super(vehicleControl);
        this.engine = engine;
        this.gearBox = gearBox;
        this.driveTrain = driveTrain;
        shifter = new AutomaticShifter(new String[]{"PARK", "REVERSE", "NEUTRAL", "DRIVE"});
        this.throttleValue = 0;
    
    }
    
    public float getEngineRPM(){
        return engineRPM;
    }
    /**
     * 
     * @return
     */
    public int getCurrentGear(){
    return (int) currentGear;
    }    
    
    @Override
    public Shifter getShifter(){
        return shifter;
    } 
    /**
     * 
     * @return Engine used.
     */
    public Engine getEngineType(){
    return engine;
    }
    /**
     * 
     * @return Gearbox used.
     */
    public GearBox getGearBoxType(){
    return gearBox;
    }
    /**
     * 
     * @return DriveTrain used.
     */
    public DriveTrain getDriveTrainType(){
    return driveTrain;
    }
    
    /**
     * 
     * @param throttleValue a float value from 0-1 indicating throttle pressure
     * @param accelerate weather or not throttle is activated
     */
    @Override
    public void activate(float throttleValue, boolean accelerate){  // Altered by PropulsionController
        this.throttleValue = throttleValue;
        this.accelerate = accelerate;
    }
    
    @Override
    public void gearUp(){
        if (currentGear + 1 < gearBox.getGearRatios().size())
            currentGear++;
    }
    
    @Override
    public void gearDown(){
        if (currentGear >= 1)
            currentGear--;
    }
    
    /**
     * 
     * @param t = physicsTick time
     */
    @Override
    public void update(float t){  // Updated from physicsTickListener
        float driveAngle = vehicleControl.getLinearVelocity().angleBetween(vehicleControl.getForwardVector(null));
        int direction = 2;
        float gasValue, brakeValue;
        
        if (throttleValue > 0) {
            gasValue = throttleValue;
            brakeValue = 0;
        }
        else if (throttleValue < 0) {
            brakeValue = Math.abs(throttleValue);
            gasValue = 0;
        }
        else
            gasValue = brakeValue = 0;
        //System.out.println(Math.abs(driveAngle));
        
        
        if (Math.abs(driveAngle - (FastMath.PI / 2f)) < .1f) {
            //System.out.println("Stopped");
            direction = 0;
        }
        else if ((FastMath.PI / 2f)- driveAngle > 0) {
            //System.out.println("Moving forwards");
            direction = 1;
        } else {
            //System.out.println("Moving backwards");
            direction = -1;
        }
            
        
        
        speed = FastMath.abs(vehicleControl.getLinearVelocity().dot(vehicleControl.getForwardVector(null).normalize()));    
        float Ftraction = 0, Fdrag = 0, Frr = 0, FdragMag, FrrMag, FtractionNet = 0, Fbrake = 0;    
        //Vector3f forward = vehicleControl.getForwardVector(null).normalize();
        float gearRatio = gearBox.getRatioFromGear(currentGear);
        engineRPM = getDriveWheelRevPerSecAverage(speed) * gearRatio * 2.86f * 60f;
        if (engineRPM < engine.getIdleRpm())
            engineRPM = engine.getIdleRpm();

        //float Cdrag = .1f * .3f * 2.2f * 1.29f;
        float Cdrag = .25f;
        float Crr = 0;
        FdragMag = Cdrag * (float)Math.pow(speed, 2);
        FrrMag = Crr * speed;
        
        torque = gasValue * engine.getTorqueAtRPM(engineRPM, accelerate);
        float radius = 0.2286f;
        Ftraction = (torque * gearRatio * 2.86f * .7f) / radius;
        Fbrake = 0;


        if (shifter.getGear().equals("DRIVE")){ //in Drive
            if (currentGear < 1)
                currentGear = 1;
            
            
            
            /*if (direction == 1) {
                
            }
          

            if (throttleValue > 0){    
                
                Fbrake = 0;

            } else if (throttleValue < 0 && speed > .1f) {  
                Fbrake = -3000;
                Ftraction = 0;
            } else if (throttleValue < 0){
                Fbrake = 0;
                Ftraction = 0;
            } else if (throttleValue == 0){
                Ftraction = Fbrake = 0;
            }*/
            
            if (engineRPM >= engine.getShiftUpRpsLimit() * 60f && currentGear > 0)
                gearUp();

            if (engineRPM <= 60f * engine.getShiftDownRpsLimit() && currentGear > 1)
                gearDown();

        } else if (shifter.getGear().equals("REVERSE")) {
            if (currentGear > 0)
                currentGear = 0;
            
            Ftraction *= -1;

            /*if (throttleValue > 0){    
                Ftraction *= -1;
                Fbrake = 0;
            } else if (throttleValue < 0 && speed > .1f) {  
                Fbrake = 3000;
                Ftraction = 0;
            } else if (throttleValue < 0){
                Fbrake = 0;
                Ftraction = 0;
                //speed = 0;
            } else if (throttleValue == 0){
                Ftraction = Fbrake = 0;
            }*/

        } else if (shifter.getGear().equals("PARK")) {
            engineRPM = gasValue * (engine.getMaxRpm() - engine.getIdleRpm()) + engine.getIdleRpm();
            Ftraction = 0;
            //Fbrake += -10000;
        } else if (shifter.getGear().equals("NEUTRAL")) {
            engineRPM = gasValue * (engine.getMaxRpm() - engine.getIdleRpm()) + engine.getIdleRpm();
            Ftraction = 0;
        }
        
        //System.out.println(direction);
        switch (direction) {
            case -1:
                if (shifter.getGear().equals("PARK"))
                    Fbrake += 1000;
                
                if (brakeValue > 0)
                    Fbrake += 3000f * brakeValue;
                
                Fdrag = FdragMag;
                Frr = FrrMag;
                
                break;
            case 0:
                if (speed < .05f)
                    vehicleControl.setLinearVelocity(new Vector3f(0, vehicleControl.getLinearVelocity().getY(), 0));
                //System.out.println("STOPPED");
                break;
            case 1:
                if (shifter.getGear().equals("PARK"))
                    Fbrake += -1000;
                
                //System.out.println(brakeValue);
                
                if (brakeValue > 0)
                    Fbrake += -3000f * brakeValue;
                
                Fdrag = -FdragMag;
                Frr = -FrrMag;
                break;
            default:
                if (brakeValue > 0)
                    Fbrake += -3000f * brakeValue;
                break;
            
        }
        
 
        FtractionNet = Ftraction + Fbrake + Fdrag + Frr;


        

       
        addVehicleTorque(FtractionNet,accelerate,speed);
    }
    
    public float getDriveWheelRadiusAverage(){
        float totalRadius = 0;
        int driveWheels = 0;
        
        for(int i=0;i<vehicleControl.getNumWheels();i++){
            if(vehicleControl.getWheel(i).getWheelInfo().bIsFrontWheel){
            totalRadius+=vehicleControl.getWheel(i).getRadius();
            driveWheels++;
            }
        }
        
        return totalRadius / (float) driveWheels;
    }

    public float getDriveWheelRpsAverage(float velocity){
    
    
    float totalDriveWheelDistance = 0;
    int driveWheels = 0;
        for(int i=0;i<vehicleControl.getNumWheels();i++){
            if(vehicleControl.getWheel(i).getWheelInfo().bIsFrontWheel){
            totalDriveWheelDistance+=vehicleControl.getWheel(i).getRadius()*FastMath.TWO_PI;
            driveWheels++;
            }
        }
    return (velocity/(totalDriveWheelDistance/driveWheels));
    }
    
    //Velocity in meters/second
    public float getDriveWheelRevPerSecAverage(float velocity){  
    float totalDriveWheelDiameter = 0;
    float averageDiameter = 0, avgCircumference = 0;
    int driveWheels = 0;
        for(int i=0;i<vehicleControl.getNumWheels();i++){
            if(vehicleControl.getWheel(i).getWheelInfo().bIsFrontWheel){
            totalDriveWheelDiameter+=vehicleControl.getWheel(i).getRadius()*2;
            driveWheels++;
            }
        }
    averageDiameter = (totalDriveWheelDiameter/driveWheels);
    avgCircumference = averageDiameter * FastMath.PI;
    
    return velocity / avgCircumference;

    }
    
    /**
     * 
     * @param totalTorque torque to add to vehicle drivewheels
     * @param accelerate accelerating/decelerating
     * @param velocity determines what direction the vehicle is moving
     */
    public void addVehicleTorque(float totalTorque,boolean accelerate,float velocity){
        if(accelerate)
            vehicleControl.accelerate(totalTorque);   
        else{
            if(velocity>0)
            vehicleControl.accelerate(totalTorque);
            else
            vehicleControl.accelerate(-totalTorque);
        }
    }
    
    public float getSpeed(){
        return speed;
    }
    
    public float getTorque() {
        return torque;
    }

    public void setTorque(float torque) {
        this.torque = torque;
    }
}
