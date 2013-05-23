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

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import createacity.drivetrain.*;
import java.util.ArrayList;
//import createacity.ai.*;

/**
 *
 * A driveable "FancyCar" object.
 *
 */

public class Vehicle {

    private VehicleControl player;
    private float wheelRadius;
    //private float steeringValue=0;
    private float accelerationValue=0;
    private Vector3f defaultLocation, defaultRotation, dimensions;
    private Node carNode;
    private boolean isAI;
    
    private GearBox gearbox;
    private Engine engine;
    private DriveTrain drivetrain;
    private InternalCombustionPropulsionUnit combustionPropUnit;
    
    private boolean steering = false, pedalDepressed = false;
    private float steerDeadzone = .1f;
    private float accelerateDeadzone = .1f;
    private float brakeDeadzone = .4f;
    private float steeringWheelRecoveryRate = (float)Math.PI * 2;
    private float steerValue = 0f;
    private float steeringRatio = 15f;
    private float steeringWheelAngle = 0f;
    private float steeringWheelTurnRate = (float)Math.PI * 2;
    private float wheelAngle = 0f;
    Geometry body;

 /**
 * Creates a driveable "FancyCar" object at default location (0, 0, 0).
 * The url argument must specify an absolute
  * @param settings
  * @param inputManager
  * @param viewPort
  * @param assetManager
  * @param pSpace
  * @param rootNode
  */
    public Vehicle(String name, String model, Vector3f location, Vector3f rotation, Vector3f dimensions, Float wheelRadius, InputManager inputManager, ViewPort viewPort, AssetManager assetManager, Node parentNode, PhysicsSpace pSpace){

        defaultLocation = location.clone();
        defaultRotation = rotation.clone();
        
        isAI = name.equals("AI");
        
        this.wheelRadius = wheelRadius;
        this.dimensions = dimensions;
        
        buildPlayer(name, model, assetManager, parentNode, pSpace);
        setupDriveTrain(inputManager, pSpace);

    }

    
    private void setupDriveTrain(InputManager inputManager, PhysicsSpace pSpace){
        gearbox = new GearBox();
        gearbox.addGearRatio(0, 2.940f);   // Gear 0 is reverse
        gearbox.addGearRatio(1, 4.584f);
        gearbox.addGearRatio(2, 2.964f);
        gearbox.addGearRatio(3, 1.912f);
        gearbox.addGearRatio(4, 1.440f);
        gearbox.addGearRatio(5, 1.000f);
        gearbox.addGearRatio(6, .746f);
        gearbox.setGearShiftTime(.5f);
        
        engine = new Engine();
        engine.setIdleRpm(750);    
        engine.setStartRpm(1500);   // Speed when clutch is used
        engine.setMaxRpm(7000);
        engine.setShiftDownRpmLimit(1500);
        engine.setShiftUpRpmLimit(6500);
        engine.addTorqueInfo(0, 0f);  // Engine brakepower
        engine.addTorqueInfo(1,122f);
        engine.addTorqueInfo(2,197f);
        engine.addTorqueInfo(3,197f);
        engine.addTorqueInfo(4,197f);
        engine.addTorqueInfo(5,197f);
        engine.addTorqueInfo(6,162f);
        engine.addTorqueInfo(7,176f);
        
        drivetrain = new DriveTrain();

        // This (.21f) is the meter value of a
        // a 17inch wheels wheelRadius. Later when the torque
        // is divided by this, the result is the
        // the linear force at the wheel edge.
        // Torque = Force*arm | Force = Torque / arm
        // The "arm" is the distance from the shaft to the wheel edge.        
        drivetrain.setFinalRatio(.6f);
        
        combustionPropUnit = new InternalCombustionPropulsionUnit(engine,gearbox,drivetrain,player);
        ArrayList<PropulsionUnit> propUnits = new ArrayList<>();
        propUnits.add(combustionPropUnit);
        
        
            PropulsionController controller = new PropulsionController(propUnits,"An awesome fluxCapacitorPropulsionUnit, ftw!");

            if (!isAI) {
            inputManager.addMapping(controller.getUniqueActionName(PropulsionController.accForward), new KeyTrigger(KeyInput.KEY_W));
            inputManager.addMapping(controller.getUniqueActionName(PropulsionController.accBackward), new KeyTrigger(KeyInput.KEY_S));
            inputManager.addMapping(controller.getUniqueActionName(PropulsionController.gearDown), new KeyTrigger(KeyInput.KEY_PGDN));
            inputManager.addMapping(controller.getUniqueActionName(PropulsionController.gearUp), new KeyTrigger(KeyInput.KEY_PGUP));
            inputManager.addListener(controller, controller.getUniqueActionName(PropulsionController.accForward),controller.getUniqueActionName(PropulsionController.accBackward),controller.getUniqueActionName(PropulsionController.gearUp),controller.getUniqueActionName(PropulsionController.gearDown));
            }
            ((Node) player.getUserObject()).addControl(controller);
            pSpace.addTickListener(controller);
        
    }

    private void buildPlayer(String name, String model, AssetManager assetManager, Node parentNode, PhysicsSpace pSpace) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        //mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", ColorRGBA.Red);
        
        //create a compound shape and attach the BoxCollisionShape for the car body at 0,1,0
        //this shifts the effective center of mass of the BoxCollisionShape to 0,-1,0
        CompoundCollisionShape compoundShape = new CompoundCollisionShape();
        BoxCollisionShape box = new BoxCollisionShape(new Vector3f(dimensions.x * 0.5f, dimensions.y * 0.5f, dimensions.z * 0.5f));
        compoundShape.addChildShape(box, new Vector3f(0, 1, 0));
        
        //create vehicle node
        carNode =new Node(name);
        player = new VehicleControl(compoundShape, 1450);
        carNode.addControl(player);
        
        //setting suspension values for wheels, this can be a bit tricky
        //see also https://docs.google.com/Doc?docid=0AXVUZ5xw6XpKZGNuZG56a3FfMzU0Z2NyZnF4Zmo&hl=en
        float stiffness = 60.0f;//200=f1 car
        float compValue = .3f; //(should be lower than damp)
        float dampValue = .4f;
        player.setSuspensionCompression(compValue * 2.0f * FastMath.sqrt(stiffness));
        player.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
        player.setSuspensionStiffness(stiffness);
        player.setMaxSuspensionForce(10000.0f);
        
        //Create four wheels and add them at their locations
        Vector3f wheelDirection = new Vector3f(0, -1, 0); // was 0, -1, 0
        Vector3f wheelAxle = new Vector3f(-1, 0, 0); // was -1, 0, 0
        float restLength = 0.3f;
        float yOff = 0.5f;
        float xOff = 1f;
        float zOff = 2f;
        
        Cylinder wheelMesh = new Cylinder(16, 16, wheelRadius, wheelRadius * 0.6f, true);
        
        Node node1 = new Node("wheel 1 node");
        Geometry wheels1 = new Geometry("wheel 1", wheelMesh);
        node1.attachChild(wheels1);
        wheels1.rotate(0, FastMath.HALF_PI, 0);
        wheels1.setMaterial(mat);
        player.addWheel(node1, new Vector3f(-xOff, yOff, zOff),
                wheelDirection, wheelAxle, restLength, wheelRadius, true);

        Node node2 = new Node("wheel 2 node");
        Geometry wheels2 = new Geometry("wheel 2", wheelMesh);
        node2.attachChild(wheels2);
        wheels2.rotate(0, FastMath.HALF_PI, 0);
        wheels2.setMaterial(mat);
        player.addWheel(node2, new Vector3f(xOff, yOff, zOff),
                wheelDirection, wheelAxle, restLength, wheelRadius, true);

        Node node3 = new Node("wheel 3 node");
        Geometry wheels3 = new Geometry("wheel 3", wheelMesh);
        node3.attachChild(wheels3);
        wheels3.rotate(0, FastMath.HALF_PI, 0);
        wheels3.setMaterial(mat);
        player.addWheel(node3, new Vector3f(-xOff, yOff, -zOff),
                wheelDirection, wheelAxle, restLength, wheelRadius, false);

        Node node4 = new Node("wheel 4 node");
        Geometry wheels4 = new Geometry("wheel 4", wheelMesh);
        node4.attachChild(wheels4);
        wheels4.rotate(0, FastMath.HALF_PI, 0);
        wheels4.setMaterial(mat);
        player.addWheel(node4, new Vector3f(xOff, yOff, -zOff),
                wheelDirection, wheelAxle, restLength, wheelRadius, false);
        
        carNode.attachChild(node1);
        carNode.attachChild(node2);
        carNode.attachChild(node3);
        carNode.attachChild(node4);
        parentNode.attachChild(carNode);
        pSpace.add(player);

        //player.setKinematic(true);
        player.setPhysicsLocation(defaultLocation);
        carNode.setLocalTranslation(defaultLocation);

        Matrix3f rot = new Matrix3f();
        rot.fromAngleAxis(defaultRotation.x, Vector3f.UNIT_X);
        rot.fromAngleAxis(defaultRotation.y, Vector3f.UNIT_Y);
        rot.fromAngleAxis(defaultRotation.z, Vector3f.UNIT_Z);
        carNode.rotate(defaultRotation.x, defaultRotation.y, defaultRotation.z);
        
        player.setPhysicsRotation(rot);
        carNode.addControl(player);
        
        
        //if (!CityApplication.DEBUG) {
            Box bodyBox = new Box(Vector3f.ZERO.add(0, dimensions.getY() / 2f, 0), dimensions.getX() / 2f, dimensions.getY() / 2f, dimensions.getZ() / 2f);
            
            Material bodyMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            bodyMat.setColor("Color", ColorRGBA.Red);
            body = new Geometry("Car Body", bodyBox);
            body.setMaterial(bodyMat);
            parentNode.attachChild(body);
        //}
            
    }

    public void reset() {
        System.out.println("Reset");
                player.setPhysicsLocation(defaultLocation);
                Quaternion q = new Quaternion();
                q.fromAngles(defaultRotation.getX(), defaultRotation.getY(), defaultRotation.getZ());
                player.setPhysicsRotation(q);
                carNode.setLocalTranslation(defaultLocation);

                Matrix3f rot = new Matrix3f();
                rot.fromAngleAxis(defaultRotation.x, Vector3f.UNIT_X);
                rot.fromAngleAxis(defaultRotation.y, Vector3f.UNIT_Y);
                rot.fromAngleAxis(defaultRotation.z, Vector3f.UNIT_Z);
                carNode.rotate(defaultRotation.x, defaultRotation.y, defaultRotation.z);
                //player.setPhysicsRotation(rot);
                
                player.setLinearVelocity(Vector3f.ZERO);
                player.setAngularVelocity(Vector3f.ZERO);
                player.resetSuspension();
    }
    
    public void update(float tpf) {
        if (!isAI) {
            if (pedalDepressed) {
                pedalDepressed = false;
            }
            else {
                combustionPropUnit.activate(0, false);
            }

            if (steering) {
                steering = false;
            }
            else {
                if (FastMath.abs(steeringWheelAngle - (steeringWheelRecoveryRate * tpf)) <= 0) {
                            steerValue = 0;
                }
                else {
                    if (FastMath.sign(steeringWheelAngle) == 1) {
                        steeringWheelAngle -= steeringWheelRecoveryRate * tpf;
                        
                    }
                    else if (FastMath.sign(wheelAngle) == -1) {
                        steeringWheelAngle += steeringWheelRecoveryRate * tpf;
                    }
                }
                
            }
        }
        
        wheelAngle = steeringWheelAngle/steeringRatio;
        
        player.steer(wheelAngle);

        
        if (body != null) {
           body.setLocalTranslation(player.getPhysicsLocation().add(new Vector3f(0, wheelRadius, 0)));
           body.setLocalRotation(player.getPhysicsRotation());
        }
    }
    
    public void throttlePressed(float value) {
        pedalDepressed = true;
        if (value >= accelerateDeadzone) {
            combustionPropUnit.activate(value, true);
        } else {
            combustionPropUnit.activate(0, false);
        }
    }
    
    public void brakePressed(float value) {
        pedalDepressed = true;
        if (value >= brakeDeadzone) {
            combustionPropUnit.activate(-value, false);
        } else {
            combustionPropUnit.activate(0, false);
        }
    }
    
    public void steer(float value, float tpf) {
     
        steeringWheelAngle += value * tpf * steeringWheelTurnRate;
        
        if (steeringWheelAngle > (float)Math.PI * 4f) {
            steeringWheelAngle = (float)Math.PI * 4f;
        }
        
        if (steeringWheelAngle < (float)Math.PI * -4f) {
            steeringWheelAngle = (float)Math.PI * -4f;
        }
        
        
        //if (value >= steerDeadzone){
            steering = true;
        //}
            
    }

    public VehicleControl getPlayer() {
        return player;
    }

    public void setPlayer(VehicleControl player) {
        this.player = player;
    }

    public float getAccelerationValue() {
        return accelerationValue;
    }

    public void setAccelerationValue(float accelerationValue) {
        this.accelerationValue = accelerationValue;
    }

    public float getSteeringValue() {
        return steerValue;
    }

    public void setSteeringValue(float steeringValue) {
        this.steeringWheelAngle = steeringValue;
    }

    public Node getCarNode() {
        return carNode;
    }

    public void setCarNode(Node carNode) {
        this.carNode = carNode;
    }
    
    public InternalCombustionPropulsionUnit getCombustionPropUnit() {
        return combustionPropUnit;
    }
    
    public Vector3f getDimensions() {
        return dimensions;
    }

    public void setDimensions(Vector3f dimensions) {
        this.dimensions = dimensions;
    }
}