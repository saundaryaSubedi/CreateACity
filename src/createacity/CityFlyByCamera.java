//Copyright 2011 New York City 3D Community

//This file is part of New York City 3D.

//New York City 3D is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//New York City 3D is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with New York City 3D.  If not, see <http://www.gnu.org/licenses/>.
package createacity;

import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.JoyAxisTrigger;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.renderer.Camera;

/**
 * A first person view camera controller modified for joystick support.
 * After creation, you must register the camera controller with the
 * dispatcher using #registerWithDispatcher().
 *
 * Controls:
 *  - Move the mouse or joystick axis to rotate the camera
 *  - Mouse wheel for zooming in or out
 *  - WASD keys or joystick axis for moving forward/backward and strafing
 *  - QZ keys raise or lower the camera
 */
public class CityFlyByCamera extends FlyByCamera{
    private float joyMoveSpeed;
    
    public CityFlyByCamera(Camera cam){
        super(cam);
    }
    
    /**
     * Registers the FlyByCamera to receive input events from the provided
     * Dispatcher.
     * @param dispatcher
     */
    @Override
    public void registerWithInput(InputManager inputManager){
        this.inputManager = inputManager;
        
        String[] mappings = new String[]{
            "FLYCAM_Left",
            "FLYCAM_Right",
            "FLYCAM_Up",
            "FLYCAM_Down",
            
            "FLYCAM_JOY_Left",
            "FLYCAM_JOY_Right",
            "FLYCAM_JOY_Up",
            "FLYCAM_JOY_Down",

            "FLYCAM_StrafeLeft",
            "FLYCAM_StrafeRight",
            "FLYCAM_Forward",
            "FLYCAM_Backward",
            
            "FLYCAM_JOY_StrafeLeft",
            "FLYCAM_JOY_StrafeRight",
            "FLYCAM_JOY_Forward",
            "FLYCAM_JOY_Backward",

            "FLYCAM_ZoomIn",
            "FLYCAM_ZoomOut",
            "FLYCAM_RotateDrag",

            "FLYCAM_Rise",
            "FLYCAM_Lower"
        };

        // both mouse and button - rotation of cam
        inputManager.addMapping("FLYCAM_Left", new MouseAxisTrigger(MouseInput.AXIS_X, true),
                                               new KeyTrigger(KeyInput.KEY_LEFT));

        inputManager.addMapping("FLYCAM_Right", new MouseAxisTrigger(MouseInput.AXIS_X, false),
                                                new KeyTrigger(KeyInput.KEY_RIGHT));

        inputManager.addMapping("FLYCAM_Up", new MouseAxisTrigger(MouseInput.AXIS_Y, false),
                                             new KeyTrigger(KeyInput.KEY_UP));

        inputManager.addMapping("FLYCAM_Down", new MouseAxisTrigger(MouseInput.AXIS_Y, true),
                                               new KeyTrigger(KeyInput.KEY_DOWN));
        
        inputManager.addMapping("FLYCAM_JOY_Left", new JoyAxisTrigger(0, 3, true));

        inputManager.addMapping("FLYCAM_JOY_Right", new JoyAxisTrigger(0, 3, false));

        inputManager.addMapping("FLYCAM_JOY_Up", new JoyAxisTrigger(0, 2, true));

        inputManager.addMapping("FLYCAM_JOY_Down", new JoyAxisTrigger(0, 2, false));

        // mouse only - zoom in/out with wheel, and rotate drag
        inputManager.addMapping("FLYCAM_ZoomIn", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping("FLYCAM_ZoomOut", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        inputManager.addMapping("FLYCAM_RotateDrag", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        // keyboard only WASD for movement and WZ for rise/lower height
        inputManager.addMapping("FLYCAM_StrafeLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("FLYCAM_StrafeRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("FLYCAM_Forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("FLYCAM_Backward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("FLYCAM_JOY_StrafeLeft", new JoyAxisTrigger(0, 1, true));
        inputManager.addMapping("FLYCAM_JOY_StrafeRight",new JoyAxisTrigger(0, 1, false));
        inputManager.addMapping("FLYCAM_JOY_Forward", new JoyAxisTrigger(0, 0, true));
        inputManager.addMapping("FLYCAM_JOY_Backward", new JoyAxisTrigger(0, 0, false));
        inputManager.addMapping("FLYCAM_Rise", new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping("FLYCAM_Lower", new KeyTrigger(KeyInput.KEY_Z));

        inputManager.addListener(this, mappings);
        inputManager.setCursorVisible(dragToRotate);

        /*Joystick[] joysticks = inputManager.getJoysticks();
        if (joysticks != null && joysticks.length > 0){
            Joystick joystick = joysticks[0];
            joystick.assignAxis("FLYCAM_StrafeRight", "FLYCAM_StrafeLeft", JoyInput.AXIS_POV_X);
            joystick.assignAxis("FLYCAM_Forward", "FLYCAM_Backward", JoyInput.AXIS_POV_Y);
            joystick.assignAxis("FLYCAM_Right", "FLYCAM_Left", joystick.getXAxisIndex());
            joystick.assignAxis("FLYCAM_Down", "FLYCAM_Up", joystick.getYAxisIndex());
        }*/
    }
    
    @Override
    public void onAnalog(String name, float value, float tpf) {
        if (!enabled)
            return;
       
        if (name.equals("FLYCAM_Left")){
            rotateCamera(value, initialUpVec);
        }else if (name.equals("FLYCAM_JOY_Left")){
            rotateCamera(value * moveSpeed * tpf * 5, initialUpVec);
        }else if (name.equals("FLYCAM_Right")){
            rotateCamera(-value, initialUpVec);
        }else if (name.equals("FLYCAM_JOY_Right")){
            rotateCamera(-value * moveSpeed * tpf * 5, initialUpVec);
        }else if (name.equals("FLYCAM_Up")){
            rotateCamera(-value, cam.getLeft());
        }else if (name.equals("FLYCAM_JOY_Up")){
            rotateCamera(-value * moveSpeed * tpf * 5, cam.getLeft());
        }else if (name.equals("FLYCAM_Down")){
            rotateCamera(value, cam.getLeft());
        }else if (name.equals("FLYCAM_JOY_Down")){
            rotateCamera(value * moveSpeed * tpf * 5, cam.getLeft());
        }else if (name.equals("FLYCAM_Forward")){
            moveCamera(value, false);
        }else if (name.equals("FLYCAM_JOY_Forward")){
            moveCamera(value * moveSpeed * tpf, false);
        }else if (name.equals("FLYCAM_Backward")){
            moveCamera(-value, false);
        }else if (name.equals("FLYCAM_JOY_Backward")){
            moveCamera(-value * moveSpeed * tpf, false);
        }else if (name.equals("FLYCAM_StrafeLeft")){
            moveCamera(value, true);
        }else if (name.equals("FLYCAM_JOY_StrafeLeft")){
            moveCamera(value * moveSpeed * tpf, true);
        }else if (name.equals("FLYCAM_StrafeRight")){
            moveCamera(-value, true);
        }else if (name.equals("FLYCAM_JOY_StrafeRight")){
            moveCamera(-value * moveSpeed * tpf, true);
        }else if (name.equals("FLYCAM_Rise")){
            riseCamera(value);
        }else if (name.equals("FLYCAM_Lower")){
            riseCamera(-value);
        }else if (name.equals("FLYCAM_ZoomIn")){
            zoomCamera(value);
        }else if (name.equals("FLYCAM_ZoomOut")){
            zoomCamera(-value);
        }
    }
    
    public float getJoyMoveSpeed() {
        return joyMoveSpeed;
    }

    public void setJoyMoveSpeed(float joyMoveSpeed) {
        this.joyMoveSpeed = joyMoveSpeed;
    }
}