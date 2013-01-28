/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package createacity.drivetrain;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

/**
 * Implementation of an internal combustion engine drivetrain.
 * @author Vekjeft
 */
public class InternalCombustionPropulsionUnit1 extends PropulsionUnit{
    public Shifter shifter;
    private static float torqueAdjustFactor = 5f;
    private float engineTorque = 0;
    private float torqueOut = 0;
    private boolean isChangingGear = false;
    private float gearChangeCounter = 0;

    private float totalRatio = 0;
    
    private float engineRps=0;
    private float engineRpsWithClutchSim=0;
    private float wheelRps=0;
    
    private int currentGear = 1;        // Gear 0 is reverse
    private int wantedGear = 1;

    
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
    public InternalCombustionPropulsionUnit1(Engine engine,GearBox gearBox,DriveTrain driveTrain,VehicleControl vehicleControl){
    super(vehicleControl);
    this.engine = engine;
    this.gearBox = gearBox;
    this.driveTrain = driveTrain;
    shifter = new AutomaticShifter(new String[]{"PARK", "REVERSE", "NEUTRAL", "DRIVE"});
    }
    /**
     * 
     * @return
     */
    public int getRpm(){
    return (int) engineRpsWithClutchSim*60;
    }
    /**
     * 
     * @return
     */
    public int getCurrentGear(){
    return (int) currentGear;
    }    
    
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
        shifter.gearUp();
        currentGear++;
    }
    
    @Override
    public void gearDown(){
        shifter.gearDown();
        currentGear--;
    }
    /**
     * 
     * @param t = physicsTick time
     */
    @Override
    public void update(float t){  // Updated from physicsTickListener
    Vector3f vel = new Vector3f();
    vehicleControl.getForwardVector(vel);
    
    float velocity = vehicleControl.getLinearVelocity().dot(vel);
    
        totalRatio = gearBox.getRatioFromGear(currentGear)*driveTrain.getFinalRatio();
        wheelRps = getDriveWheelRpsAverage(velocity);
        engineRpsWithClutchSim = engineRps = FastMath.abs(wheelRps/totalRatio); // A crude clutch simulation.
        
        if(isChangingGear==false&&throttleValue>=0&&velocity<-3)
        {
            isReversing =false;
            if(wantedGear==0){
            wantedGear = 1;
            isChangingGear = true;
            }
            else if(engineRpsWithClutchSim>engine.getShiftUpRpsLimit()&&wantedGear<gearBox.getNumberOfGears()-1){
            isChangingGear = true;
            wantedGear++;
            }
            else if(engineRps<engine.getShiftDownRpsLimit()&&!accelerate&&wantedGear>1){
            isChangingGear = true;
            wantedGear--;
            }

        }
        else if(isChangingGear==false&&throttleValue<0&&velocity>2){
            isReversing = true;
            wheelRps = FastMath.abs(wheelRps);
            engineRpsWithClutchSim = FastMath.abs(engineRpsWithClutchSim);
            wantedGear=0;
            currentGear=0;
        }
        else if(velocity<-3){
        gearChangeCounter+=t;
            if(gearChangeCounter>gearBox.getGearShiftTime()){
            isChangingGear = false;
            gearChangeCounter = 0;
            currentGear = wantedGear;
            }
        }
        if(engineRpsWithClutchSim<engine.getStartRps()&&accelerate&&currentGear<2){
        engineRpsWithClutchSim=engine.getStartRps();
        }
        else if(engineRpsWithClutchSim<engine.getIdleRps())
        {
        engineRpsWithClutchSim=engine.getIdleRps();
        }        
        if(isChangingGear)
        {
        torqueOut = 0;
        }
        else{            
        engineTorque = (accelerate) ? engine.getTorqueAtRPS(engineRpsWithClutchSim, true)*throttleValue : engine.getTorqueAtRPS(engineRps, false);
        torqueOut = (engineTorque/totalRatio)*torqueAdjustFactor;
        }
        addVehicleTorque(torqueOut,accelerate,velocity);
    }

    /**
     * 
     * @param velocity in m/s
     * @return rps avereage for drivewheels on the vehicle
     */
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
    
    /**
     * 
     * @param totalTorque torque to add to vehicle drivewheels
     * @param accelerate accelerating/decelerating
     * @param velocity determines what direction the vehicle is moving
     */
    public void addVehicleTorque(float totalTorque,boolean accelerate,float velocity){
        System.out.println("Adding " + totalTorque + " torque");
        if(accelerate)
            vehicleControl.accelerate(totalTorque);   
        else{
            if(velocity>0)
            vehicleControl.accelerate(totalTorque);
            else
            vehicleControl.accelerate(-totalTorque);
        }
    }
}
