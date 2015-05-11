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
package createacity.states;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.collision.CollisionResults;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.swing.JOptionPane;
import createacity.HUD;
import createacity.IntersectionInfo;
import createacity.CityApplication;
import createacity.CityHelper;
import createacity.NodeInspector;
import createacity.Sensor;
import createacity.StreetInfo;
import createacity.ai.AIVehicle;
import createacity.intersection.DefinedSignalControl;
import createacity.intersection.Intersection;
import createacity.intersection.SignalControl;
import createacity.intersection.SignalizedIntersection;
import createacity.objects.streetlight.StreetlightUtility;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main state
 */
public class MainState extends AbstractAppState implements ActionListener {
    private static final Logger logger = Logger.getLogger(MainState.class.getName());
    public static final String MAIN_PAUSE = "Main - Pause";
    public static final String TOGGLE_STREETLIGHTS = "Toggle Streetlights";
    protected Node rootNode;
    
    private Node rawWorld, trafficSignals;
    protected Node vehicles, world;
    
    protected CityApplication app;
    protected HashMap<String, StreetInfo> streetInfoMap;
    protected HashMap<String, IntersectionInfo> intersectionInfoMap;
    
    private DirectionalLight sun;
    private Vector3f lightDir = new Vector3f(-.5f, -.5f, -.5f); // same as light source
     
    public static final float FLYCAM_SPEED = 50f;
    
    private AIVehicle testAI;
    protected HUD hud;
   
    private float min = 1, max = -1;
    
    public static ScheduledThreadPoolExecutor executor;
    
    public static final int STREETLIGHTS_INDEX = 0;
    public static final int ROADS_INDEX = 1;
    
    public static final boolean firstPersonAICam = false;
    public Vector3f loc = Vector3f.ZERO;
    private String closestRoad;
    
    protected FlyByCamera flyCam;
    public static boolean findingClosestRoad = false;
    protected ArrayList<Sensor> sensors;
    private ArrayList<Intersection> trafficSignalList;
    private ArrayList<DefinedSignalControl> definedSignalsControlList;
    private ArrayList<SignalizedIntersection> signalizedIntersectionList;
    
    protected BulletAppState bulletAppState;
    private PointLight al;
    
    private boolean streetlightsOn;
    
    public MainState(CityApplication app){
        this.app = (CityApplication) app;   
        
        this.testAI = this.app.getTestAIVehicle();
        this.hud = this.app.getHUD();
        this.rootNode = this.app.getRootNode();
        this.rawWorld = this.app.getRawWorldNode();
        this.flyCam = this.app.getFlyByCamera();
        this.bulletAppState = this.app.getBulletAppState();
        this.al = this.app.getAl();
        this.streetInfoMap = this.app.getStreetInfoMap();
        this.intersectionInfoMap = this.app.getIntersectionInfoMap();
        this.definedSignalsControlList = this.app.getDefinedSignalsControlList();
        this.sensors = this.app.getSensors();
        this.sun = this.app.getSun();
        this.vehicles = this.app.getVehiclesNode();
        
        streetlightsOn = false;
        logger.log(Level.INFO, "Finished initializing");
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app){
        super.initialize(stateManager, app);   
    }
    
    private void addInputMappings() {
        InputManager inputManager = app.getInputManager();
        
        if (!inputManager.hasMapping(MAIN_PAUSE)) {
            inputManager.addMapping(MAIN_PAUSE, new KeyTrigger(KeyInput.KEY_ESCAPE));
        }
        
        if (!inputManager.hasMapping(TOGGLE_STREETLIGHTS)) {
            inputManager.addMapping(TOGGLE_STREETLIGHTS, new KeyTrigger(KeyInput.KEY_L));
        }
        
        inputManager.addListener(this, MAIN_PAUSE, TOGGLE_STREETLIGHTS);
    }
    
    private void removeInputMappings() {
        InputManager inputManager = app.getInputManager();
        
        if (inputManager.hasMapping(MAIN_PAUSE)) {
            inputManager.deleteMapping(MAIN_PAUSE);
        }
        
        if (!inputManager.hasMapping(TOGGLE_STREETLIGHTS)) {
            inputManager.deleteMapping(TOGGLE_STREETLIGHTS);
        }
        
        inputManager.removeListener(this);
    }   
    
    @Override
    public void update(float tpf){
        /*if (!app.getNifty().getCurrentScreen().getScreenId().equals("hud")) {
            app.getNifty().fromXml("hud.xml", "hud");
        }*/
        
        if (findingClosestRoad) {
            loc = CityHelper.getLocation();
            closestRoad = getNearestStreet();
            
            if(closestRoad != null){
                //.... Success! Let's process the wayList and move the NPC...
                JOptionPane.showMessageDialog(null, "Nearest street: " + closestRoad, "Nearest Street", JOptionPane.INFORMATION_MESSAGE);
                closestRoad = null;
                findingClosestRoad = false;
            }
        }               
        
        hud.update(rootNode, app.getCamera(), app.getNifty(), streetInfoMap);
        setSunDir();
        testAI.step(tpf);
        //System.out.println(app.getCamera().getLocation());
        
        if (firstPersonAICam) {
            app.getCamera().setLocation(testAI.getVehicle().getCarNode().getWorldTranslation().add(new Vector3f(0, testAI.getVehicle().getCarNode().getWorldScale().getY() + 1, 0)));
            app.getCamera().setRotation(testAI.getVehicle().getCarNode().getWorldRotation());
        }
        
        IntersectionInfo intersection = intersectionInfoMap.get("NorthAve_EastAve_1");
        Integer[] intersectionInt = intersection.getRoadIntersections().get("NorthAve");
        
        updateMap();
        
        for(Sensor sensor: sensors) {
            sensor.check(vehicles, tpf);
        }
        //System.out.println("Sensor activated? " + sensor.isHot());
        
        for(SignalControl s: definedSignalsControlList) {
            s.update(tpf, rawWorld);
        }
        
        rootNode.updateLogicalState(tpf);
        rootNode.updateGeometricState();
    }   
    
    private void updateMap() {
        Vector2f realMapCoords = getRealMapCoords();
    }
    
    private Vector2f getRealMapCoords() {
        Vector2f currentLoc = new Vector2f(app.getCamera().getLocation().getX(), app.getCamera().getLocation().getZ());
        return currentLoc;
    }
    
    protected void setSunDir(){
        Calendar cal = Calendar.getInstance();
        int t=(cal.get(Calendar.HOUR_OF_DAY) * 3600) + (cal.get(Calendar.MINUTE) * 60) + cal.get(Calendar.SECOND);
        float theta= t * (FastMath.PI/43200f);
        float x = -FastMath.cos(theta - (FastMath.PI/2f));
        float y = -FastMath.sin(theta - (FastMath.PI/2f));
        lightDir.set(x, y, 0);

        if (cal.get(Calendar.HOUR_OF_DAY) > 17)
            sun.setColor(ColorRGBA.Orange);
        
        if (cal.get(Calendar.HOUR_OF_DAY) > 0 && cal.get(Calendar.HOUR_OF_DAY) < 6)
            sun.setColor(ColorRGBA.Black);
        
        if (cal.get(Calendar.HOUR_OF_DAY) > 6 && cal.get(Calendar.HOUR_OF_DAY) < 17)
            sun.setColor(ColorRGBA.White);
        
        sun.setDirection(lightDir);
    }
            
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        
        if (name.equals(MAIN_PAUSE) && isPressed) {
                
            //app.getStateManager().detach(app.mainState);
            setEnabled(false);
            app.getPauseState().setEnabled(true);
            //app.getInputManager().clearMappings();
            //app.getNifty().fromXml("gui/hud.xml", "pause");
            //Element exitPopup = app.getNifty().createPopup("exitPopup");
                    
        }
        
        if (name.equals(MAIN_PAUSE) && !isPressed) {
            bulletAppState.setEnabled(true);
            
        }
        
        if (name.equals(TOGGLE_STREETLIGHTS) && !isPressed) {
            if (streetlightsOn) {
                streetlightsOn = false;
                rootNode.addLight(sun);
                rootNode.removeLight(al);
                StreetlightUtility.turnOffAllStreetlights(rootNode);
            } else {
                streetlightsOn = true;
                rootNode.removeLight(sun);
                rootNode.addLight(al);
                StreetlightUtility.turnOnAllStreetlights(rootNode);
                
            }
            
        }
            
    }
    
    public String getNearestStreet() {
            //Read or write data from the scene graph -- via the execution queue:
            //Node worldNode = (Node)world.clone(true);
            String nearestStreet = "null";
            CollisionResults results = new CollisionResults();
            float closestDistance = 101f;
            for(float xDir = -1; xDir <= 1; xDir += .1f) {
                for(float yDir = -1; yDir <= 1; yDir += .1f) {
                    for(float zDir = -1; zDir <= 1; zDir += .1f) {                   
                        Ray ray = new Ray(loc, new Vector3f(xDir, yDir, zDir));
                        world.collideWith(ray, results);
                        for (int i = 0; i < results.size(); i++) {
                            float dist = results.getCollision(i).getDistance();
                            String name = results.getCollision(i).getGeometry().getParent().getName();
                            if (name.startsWith("R_") && dist <= 100 && dist < closestDistance) {
                                nearestStreet = name;
                                closestDistance = dist;
                            }
                        }
                    }
                }
            }

            return nearestStreet;
        
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        
        if (enabled) {
            app.getNifty().fromXml("hud.xml", "hud");
            bulletAppState.setEnabled(true);
            flyCam.setEnabled(true);
            flyCam.setDragToRotate(false);
            flyCam.registerWithInput(app.getInputManager());
            addInputMappings();
        } else {
            flyCam.unregisterInput();
            flyCam.setEnabled(false);
            bulletAppState.setEnabled(false);
            removeInputMappings();
            app.getNifty().removeScreen("hud");
        }
    }
    
    @Override
    public void stateAttached(AppStateManager stateManager) { 
        
    }
    
    @Override
    public void stateDetached(AppStateManager stateManager) {       
        
    }  
}