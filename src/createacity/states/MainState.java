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
import com.jme3.asset.BlenderKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResults;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.UserData;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.util.SkyFactory;
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
import createacity.intersection.DefinedSignalControlUtility;
import createacity.intersection.Intersection;
import createacity.intersection.SignalizedIntersection;
import createacity.intersection.SignalizedIntersectionUtility;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main state
 */
public class MainState extends AbstractAppState implements ActionListener{
    public static final String MAIN_PAUSE = "Main - Pause";
    protected Node rootNode = new Node("MainState Root Node");
    Node rawWorld, world, worldClone, vehicles, trafficSignals;
    protected CityApplication app;
    protected HashMap<String, StreetInfo> streetInfoMap;
    protected HashMap<String, IntersectionInfo> intersectionInfoMap;
    protected HUD hud;
    DirectionalLight sun;
    SpotLight spot;
    private Vector3f lightDir = new Vector3f(-.5f, -.5f, -.5f); // same as light source
    BasicShadowRenderer bsr;
    protected FlyByCamera flyCam; 
    private final float FLYCAM_SPEED = 100;
    protected BulletAppState bulletAppState;
   
    private float min = 1, max = -1;
    
    AIVehicle testAI;
    
    public static ScheduledThreadPoolExecutor executor;
    
    public static final int STREETLIGHTS_INDEX = 0;
    public static final int ROADS_INDEX = 1;
    
    public static final boolean firstPersonAICam = false;
    public Vector3f loc = Vector3f.ZERO;
    String closestRoad;
    
   
    public static boolean findingClosestRoad = false;
    protected ArrayList<Sensor> sensors;
    private ArrayList<Intersection> trafficSignalList;
    private ArrayList<DefinedSignalControl> definedSignalsControlList;
    private ArrayList<SignalizedIntersection> signalizedIntersectionList;
    
    public MainState(CityApplication app){
        this.app = (CityApplication) app;

        InputManager inputManager = app.getInputManager();               
        
        flyCam = new FlyByCamera(app.getCamera());
        flyCam.setMoveSpeed(FLYCAM_SPEED);
        flyCam.setDragToRotate(false);
        flyCam.registerWithInput(inputManager);       
        
        hud = new HUD(this.app.getSettings());
        
        //rootNode.attachChild(SkyFactory.createSky(app.getAssetManager(), "Scenes/Beach/FullskiesSunset0068.dds", false));
        
        BlenderKey blenderKey = new BlenderKey("Models/City/City.blend");
        blenderKey.setLoadObjectProperties(true);
        rawWorld = (Node)app.getAssetManager().loadAsset(blenderKey);
        
        /* This constructor creates a new executor with a core pool size of 4. */
        executor = new ScheduledThreadPoolExecutor(4);
        
        bulletAppState = new BulletAppState();
        app.getStateManager().attach(bulletAppState);
        if (CityApplication.DEBUG) {
            bulletAppState.getPhysicsSpace().enableDebug(app.getAssetManager());
        }
        
        rawWorld.setName("World");
        rawWorld.setShadowMode(ShadowMode.CastAndReceive);
        
        vehicles = new Node("Vehicles");
        rootNode.attachChild(vehicles);
        
        //app.getNifty().registerS
        //app.getNifty().registerScreenController(app.pauseState);
        
        Logger.getLogger("").setLevel(Level.SEVERE);
        
        initWorld();
        rootNode.attachChild(rawWorld);
        PointLight al = new PointLight();
        al.setColor(ColorRGBA.White.mult(.5f));
        al.setPosition(new Vector3f(0, 12, 8));
        rootNode.addLight(al);
        CollisionShape level_shape = CollisionShapeFactory.createMeshShape(rawWorld);
        
        RigidBodyControl cityControl = new RigidBodyControl(level_shape, 0);
        rawWorld.addControl(cityControl);
        bulletAppState.getPhysicsSpace().add(cityControl);
        
        rootNode.attachChild(SkyFactory.createSky(
            app.getAssetManager(), "Textures/Sky/Bright/BrightSky.dds", false));
        
        //flyCam.unregisterInput();
        //flyCam.setEnabled(false);
       

        //carCam.setDefaultDistance(10f);
        //carCam.setMaxDistance(15f);
        bsr = new BasicShadowRenderer(app.getAssetManager(), 256);
        //app.getViewPort().addProcessor(bsr);
        rootNode.setShadowMode(ShadowMode.Off);
        initSun();
        app.getCamera().setFrustumFar(5000);
        app.getCamera().setLocation(new Vector3f(90, 10, -112f));
        
        spot = new SpotLight();
        spot.setSpotRange(100f);                           // distance
        spot.setSpotInnerAngle(15f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
        spot.setSpotOuterAngle(35f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
        spot.setColor(ColorRGBA.White.mult(1.3f));         // light color
        spot.setPosition(app.getCamera().getLocation());               // shine from camera loc
        spot.setDirection(app.getCamera().getDirection());             // shine forward from camera loc
        rootNode.addLight(spot);
        
        testAI = new AIVehicle(rootNode, world, new Vector3f(-107, 0, 95f), new Vector3f(0, FastMath.PI, 0), new Vector3f(1.79578f, 1.47574f, 4.5974f), app.getInputManager(), app.getViewPort(), app.getAssetManager(), vehicles, bulletAppState.getPhysicsSpace());
        app.getCamera().lookAt(testAI.getVehicle().getPlayer().getPhysicsLocation(), Vector3f.UNIT_Y);
        testAI.reorient();
        testAI.generateRandomDestination();
        
        closestRoad = null;
        
        if (CityApplication.DEBUG) {
            bulletAppState.getPhysicsSpace().enableDebug(app.getAssetManager());    
        }
        
        
        
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app){
        super.initialize(stateManager, app); 
        
        
        
        
    }
    
    private void addInputMappings() {
        InputManager inputManager = app.getInputManager();
        inputManager.addMapping(MAIN_PAUSE, new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(this, MAIN_PAUSE);
    }
    
    private void removeInputMappings() {
        InputManager inputManager = app.getInputManager();
        inputManager.deleteMapping(MAIN_PAUSE);
        inputManager.removeListener(this);
    }
    
    private void initWorld() {
        streetInfoMap = new HashMap<>();
        intersectionInfoMap = new HashMap<>();
        sensors = new ArrayList<>();
        trafficSignalList = new ArrayList<>();
        
        //if (!CityApplication.DEBUG) {
            adjustPhysics();
        //}
        
        definedSignalsControlList = DefinedSignalControlUtility.addControls("trafficSignalControls.txt");
        world = NodeInspector.buildNode(rootNode, rawWorld, streetInfoMap, intersectionInfoMap, sensors);
        signalizedIntersectionList = SignalizedIntersectionUtility.buildIntersections(definedSignalsControlList, (Node)world.getChild("Traffic Signals"));
        
        System.out.println("Num of intersections: " + signalizedIntersectionList.size());
        
        //NodeInspector.inspectNode(0, (Spatial)world);
    
    }
    
    private void adjustPhysics() {
        for(int i = 0; i < rawWorld.getChildren().size(); i++) {
            if (rawWorld.getChild(i).getName().startsWith("RL_")) {
                rawWorld.getChild(i).setUserData(UserData.JME_PHYSICSIGNORE, true);
                for(int j = 0; j < ((Node)rawWorld.getChild(i)).getChildren().size(); j++) {
                    ((Node)rawWorld.getChild(i)).getChild(j).setUserData(UserData.JME_PHYSICSIGNORE, true);
                }
            }
        }
    }
    
    
    
    private void initSun(){
        /** A white, directional light source */
        sun = new DirectionalLight();
        sun.setDirection(lightDir);
        sun.setColor(ColorRGBA.White.clone().multLocal(1.7f));
        rootNode.addLight(sun);
    
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
        
        spot.setPosition(app.getCamera().getLocation());
        spot.setDirection(app.getCamera().getDirection());
        
        IntersectionInfo intersection = intersectionInfoMap.get("NorthAve_EastAve_0");
        Integer[] intersectionInt = intersection.getRoadIntersections().get("NorthAve");
        
        updateMap();
        
        for(Sensor sensor: sensors) {
            sensor.check(vehicles, tpf);
        }
        //System.out.println("Sensor activated? " + sensor.isHot());
        
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
        bsr.setDirection(lightDir.normalizeLocal()); //Update shadow direction
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
    
    /**
     * Retrieves root node
     * @return the root node
     *
     */
    public Node getRootNode(){
        return rootNode;
    }
    
    /**
     * Retrieves the FlyCam object
     * @return the FlyCam object
     *
     */
    public FlyByCamera getFlyByCamera() {
        return flyCam;
    }    
}