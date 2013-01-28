/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package createacity.states;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.ChaseCamera;
import createacity.drivetrain.*;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.JoyAxisTrigger;
import com.jme3.input.controls.JoyButtonTrigger;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.ArrayList;
import createacity.CityApplication;
import createacity.Vehicle;

/**
 *
 * @author Joseph
 */
public class DrivingState extends MainState implements ActionListener{
    
    private enum VEHICLE_CAMERAS{THIRD, FIRST};    
    private VEHICLE_CAMERAS camSetting;
    //private long timeElapsedSinceOverriden = 0;
    private static final boolean SHOW_CAR_STATS = true;
   
    
    private Vehicle car;
    private ChaseCamera carCam;
    
    public static float speedMPH;
    
    
    
    public DrivingState(CityApplication app) {
        super(app);
        
        car = new Vehicle("Corolla", "Ferrari", new Vector3f(-107f, .75f, 110f), new Vector3f(0, FastMath.PI, 0), new Vector3f(1.79578f, 1.47574f, 4.5974f), 0.2286f, app.getInputManager(), app.getViewPort(), app.getAssetManager(), vehicles, bulletAppState.getPhysicsSpace());
        car.getCombustionPropUnit().getShifter().setGear("DRIVE");
        
        camSetting = VEHICLE_CAMERAS.THIRD;
        
        carCam = new ChaseCamera(app.getCamera(), car.getCarNode(), app.getInputManager());
        carCam.setUpVector(Vector3f.UNIT_Y);
        carCam.setEnabled(true);
        carCam.setSmoothMotion(true);
        carCam.setMaxDistance(100);
        carCam.setTrailingEnabled(true);
        
        app.getNifty().fromXml("hud.xml", "hud");

        carCam.setDefaultHorizontalRotation(FastMath.PI);
        carCam.setDefaultVerticalRotation((1/5f));
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app){
        super.initialize(stateManager, app); 
        flyCam.setEnabled(false);
        
        InputManager inputManager = app.getInputManager();
        //flyCam.setEnabled(true);
        //flyCam.registerWithInput(inputManager);
        
        inputManager.addMapping("Change Vehicle Camera View", new KeyTrigger(KeyInput.KEY_C));
        inputManager.addListener(this, "Change Vehicle Camera View");
    }
    
    @Override
    public void update(float tpf){
        
        super.update(tpf);
        hud.updateDriving(rootNode, world, car, app.getNifty(), streetInfoMap, intersectionInfoMap);
        //System.out.println("Steering is " + steering);
        
        
        
        car.update(tpf);
        
        switch(camSetting) {
            case FIRST:
                app.getCamera().setLocation(car.getCarNode().getWorldTranslation().add(new Vector3f(0, car.getCarNode().getWorldScale().getY() + 1, 0)));
                app.getCamera().setRotation(car.getCarNode().getWorldRotation());
                break;
        }
        
        //speedMPH = FastMath.abs(Math.signum(car.getPlayer().getCurrentVehicleSpeedKmHour()) * -1f * (3600f / 5280f) * FastMath.sqrt(FastMath.sqr(car.getPlayer().getLinearVelocity().x) + FastMath.sqr(car.getPlayer().getLinearVelocity().z)));
        if (SHOW_CAR_STATS) {
            speedMPH = car.getCombustionPropUnit().getSpeed() * 2.23693629f;
            int gear = car.getCombustionPropUnit().getCurrentGear();
            float RPM = car.getCombustionPropUnit().getEngineRPM();

            System.out.printf("%10s", car.getCombustionPropUnit().getShifter().getGear());
            System.out.printf("%5d", gear);
            System.out.printf("%5.0f", RPM);
            System.out.printf("%5.0f", car.getCombustionPropUnit().getTorque());
            System.out.printf("%7.2f", speedMPH);
            System.out.println();
        }
        rootNode.updateLogicalState(tpf);
        rootNode.updateGeometricState();
        
        
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {        
        
        
        if (name.equals("Change Vehicle Camera View") && !isPressed) {
            switch(camSetting) {
                case THIRD:
                    carCam.setEnabled(false);
                    camSetting = VEHICLE_CAMERAS.FIRST;
                    break;
                case FIRST:
                    carCam.setEnabled(true);
                    camSetting = VEHICLE_CAMERAS.THIRD;
                    break;
            }
        }
            
    }
    
    AnalogListener analogListener = new AnalogListener(){
        public void onAnalog(String name, float isPressed, float tpf) {

             /*else{
                    if (FastMath.abs(steerValue - (.25f * tpf)) <= 0)
                        steerValue = 0;
                    else {
                        if (FastMath.sign(steerValue) == 1)
                            steerValue -= .25f * tpf;
                        else if (FastMath.sign(steerValue) == -1)
                            steerValue += .25f * tpf;
                    }
                }*/

            //System.out.println(steerValue);

            //car.getPlayer().steer(steerValue);

            /*float value = isPressed * 1000f;
            float steerValue = 0;
            if (FastMath.abs(value) >= steerDeadzone)
                steerValue = value / 4f;*/

        }
    };
    
    
    
}
