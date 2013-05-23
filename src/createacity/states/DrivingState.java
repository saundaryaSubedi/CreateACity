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
public class DrivingState extends MainState implements ActionListener, AnalogListener{
    
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

        carCam.setDefaultHorizontalRotation(FastMath.PI);
        carCam.setDefaultVerticalRotation((1/5f));
    }
    
    private void addInputMappings() {
        InputManager inputManager = app.getInputManager();
        
        inputManager.addMapping("Change Vehicle Camera View", new KeyTrigger(KeyInput.KEY_C));
        inputManager.addListener(this, "Change Vehicle Camera View");
        
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(this,"Lefts");
        inputManager.addListener(this,"Rights");
        inputManager.addListener(this,"Space");
        inputManager.addListener(this,"Reset");
        
        inputManager.addMapping("Steer Left", new JoyAxisTrigger(0, 1, true), new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Steer Right", new JoyAxisTrigger(0, 1, false), new KeyTrigger(KeyInput.KEY_D));      
        inputManager.addMapping("Accelerate Vehicle", new JoyAxisTrigger(0, 4, true), new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Brake Vehicle", new JoyAxisTrigger(0, 4, false), new KeyTrigger(KeyInput.KEY_S));
        inputManager.addListener(this, new String[]{"Steer Left", "Steer Right", "Accelerate Vehicle", "Brake Vehicle"});
        inputManager.addMapping("Gear Up", new JoyButtonTrigger(0, 5), new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Gear Down", new JoyButtonTrigger(0, 4), new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addListener(this, "Gear Up", "Gear Down");
    }
    
    private void removeInputMappings() {
        InputManager inputManager = app.getInputManager();
        
        if (inputManager.hasMapping("Change Vehicle Camera View")) {
            inputManager.deleteMapping("Change Vehicle Camera View");
        }
        
        if (inputManager.hasMapping("Lefts")) {
            inputManager.deleteMapping("Lefts");
        }
        
        if (inputManager.hasMapping("Rights")) {
            inputManager.deleteMapping("Rights");
        }
        
        if (inputManager.hasMapping("Space")) {
            inputManager.deleteMapping("Space");
        }
        
        if (inputManager.hasMapping("Reset")) {
            inputManager.deleteMapping("Reset");
        }
        
        if (inputManager.hasMapping("Steer Left")) {
            inputManager.deleteMapping("Steer Left");
        }
        
        if (inputManager.hasMapping("Steer Right")) {
            inputManager.deleteMapping("Steer Right");      
        }
        
        if (inputManager.hasMapping("Accelerate Vehicle")) {
            inputManager.deleteMapping("Accelerate Vehicle");
        }
        
        if (inputManager.hasMapping("Brake Vehicle")) {
            inputManager.deleteMapping("Brake Vehicle");
        }
        
        if (inputManager.hasMapping("Gear Up")) {
            inputManager.deleteMapping("Gear Up");
        }
        
        if (inputManager.hasMapping("Gear Down")) {
            inputManager.deleteMapping("Gear Down");
        }
        
        inputManager.removeListener(this);
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
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        
        if (enabled) {
            flyCam.setEnabled(false);
            addInputMappings();
        } else {
            removeInputMappings();
        }
        
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {        
        
        super.onAction(name, isPressed, tpf);
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
        
        if (name.equals("Gear Up") && !isPressed) {
            car.getCombustionPropUnit().gearUp();
        }
        
        if (name.equals("Gear Down") && !isPressed) {
            car.getCombustionPropUnit().gearDown();
        }
        
        if (name.equals("Reset") && isPressed) {
            car.reset();
        }
            
    }
    
    @Override
    public void onAnalog(String name, float isPressed, float tpf) {
        float pos = isPressed / tpf;


            if (name.equals("Accelerate Vehicle")){
                if (car != null) {
                    car.throttlePressed(pos);
                }
            }

            if (name.equals("Brake Vehicle")) {
                if (car != null) {
                    car.brakePressed(pos);
                }
            }

            if (name.equals("Steer Left")){
                if (car != null) {
                    car.steer(pos, tpf);
                }
            }

            if (name.equals("Steer Right")){
                if (car != null) {
                    car.steer(-pos, tpf);
                }
            }
    }
    
    
    
}
            
