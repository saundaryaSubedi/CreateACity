//Copyright 2015 Create a City community

//This file is part of the open source Create a City project.

//Create a City is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//Create a City is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with Create a City.  If not, see <http://www.gnu.org/licenses/>.
package createacity;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.BlenderKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.UserData;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;
import createacity.ai.AIVehicle;
import createacity.intersection.DefinedSignalControl;
import createacity.intersection.DefinedSignalControlUtility;
import createacity.intersection.Intersection;
import createacity.intersection.SignalControl;
import createacity.intersection.SignalizedIntersection;
import createacity.intersection.SignalizedIntersectionUtility;
import createacity.objects.streetlight.StreetlightUtility;
import createacity.states.*;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.xml.xpp3.Attributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>CityApplication</code> extends the {@link com.jme3.app.SimpleApplication}
 * class to provide default functionality like a first-person camera,
 * and an accessible root node that is updated and rendered regularly, 
 * as well as features unique to createacity such as saving and loading.
 * Additionally, <code>CityApplication</code> will display a statistics view
 * using the {@link com.jme3.app.StatsView} class. It will display
 * the current frames-per-second value on-screen in addition to the statistics.
 * Several keys have special functionality in <code>CityApplication</code>:<br/>
 *
 * <table>
 * <tr><td>C</td><td>- Display the camera position and rotation in the console.</td></tr>
 * <tr><td>M</td><td>- Display memory usage in the console.</td></tr>
 * <tr><td>F5</td><td>- Show/hide fps and other useful debugging info.</td></tr>
 * </table>
 */
public class CityApplication extends SimpleApplication implements ScreenController, Controller {
        
    private MainState mainState;
    private PauseState pauseState;
    protected Nifty nifty;
    public static final boolean DEBUG = false;
    public static final String SIGNAL_PREFIX = "SIGNAL_";
    public static final String SIGNALLIGHT_PREFIX = "SIGNALLIGHT_";
    
    private static final Logger logger = Logger.getLogger(CityApplication.class.getName());
    
    private boolean load = false;
    private Future loadFuture = null;
    
    private Element progressBarElement;
    private TextRenderer textRenderer;
    
    private Node rawWorld, world, vehicles, trafficSignals;
    private HUD hud;
    private BulletAppState bulletAppState;
    private PointLight al;
    private AIVehicle testAI;
    
    private ArrayList<Sensor> sensors;    
    private ArrayList<Intersection> trafficSignalList;
    private ArrayList<DefinedSignalControl> definedSignalsControlList;
    private ArrayList<SignalizedIntersection> signalizedIntersectionList;
    
    private HashMap<String, StreetInfo> streetInfoMap;
    private HashMap<String, IntersectionInfo> intersectionInfoMap;
    private DirectionalLight sun;
    private Vector3f lightDir = new Vector3f(-.5f, -.5f, -.5f); // same as light source
 
    /* This constructor creates a new executor with a core pool size of 4. */
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(4);
    
    @Override
    public void initialize(){  
        super.initialize();
        
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
        nifty = niftyDisplay.getNifty();
        nifty.enableAutoScaling(800, 600);
        guiViewPort.addProcessor(niftyDisplay);
        
        if (inputManager.hasMapping(INPUT_MAPPING_EXIT)) {
            inputManager.deleteMapping(INPUT_MAPPING_EXIT);
        }
        
        /*Object[] options = {"Drive",
                    "Fly around"};
        int n = JOptionPane.showOptionDialog(null,
            "Would you like to drive or fly around the map?\n(Please give the city a few seconds to load)",
            "Drive or Fly?",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,     //do not use a custom Icon
            options,  //the titles of buttons
            options[0]); //default button title*/
        
        setDisplayFps(false);
        setDisplayStatView(false);

        /*if (n == 0) {
            mainState = new DrivingState(this);
        } else {
            mainState = new MainState(this);
        }*/
        pauseState = new PauseState(this);
        
        viewPort.detachScene(rootNode);
        
        stateManager.attach(pauseState);
        pauseState.setEnabled(false);
        
        nifty.fromXml("Interface/nifty_loading.xml", "loadlevel", this);
        load = true;
        
        setupKeys();           
    }
    
    private void setupKeys() {
        inputManager.addMapping("Teleport to Location", new KeyTrigger(KeyInput.KEY_T));
        inputManager.addMapping("Place Box", new KeyTrigger(KeyInput.KEY_B));
        inputManager.addMapping("Find Nearest Street", new KeyTrigger(KeyInput.KEY_F));
        
        inputManager.addListener(actionListener, "Teleport to Location", "Place Box", "Find Nearest Street");
    }
    
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String binding, boolean keyPressed, float tpf){
            if(binding.equals("Teleport to Location") && keyPressed){
                CityHelper.moveCamera(cam);
            }
            
            if(binding.equals("Place Box") && keyPressed){
                CityHelper.placeBox(rootNode, assetManager);
            }
            
            if(binding.equals("Find Nearest Street") && keyPressed){
                MainState.findingClosestRoad = true;
            }
        }
    };
    
    @Override
    public void destroy() {
        super.destroy();
        executor.shutdown();
    }
    
    public Nifty getNifty(){
        return nifty;
    }
    
    public AppSettings getSettings(){
        return settings;
    }

    @Override
    public void simpleInitApp() {
       
    }   
    
    @Override
    public void simpleUpdate(float tpf) {
       super.simpleUpdate(tpf);
       
       if (load) {
            if (loadFuture == null) {
                //if we have not started loading yet, submit the Callable to the executor
                loadFuture = executor.submit(loadingCallable);
            }
            
            //check if the execution on the other thread is done
            if (loadFuture.isDone()) {
                //these calls have to be done on the update loop thread, 
                //especially attaching the terrain to the rootNode
                //after it is attached, it's managed by the update loop thread 
                // and may not be modified from any other thread anymore!
                load = false;
                
                mainState = new MainState(this);
                stateManager.attach(mainState);    
                mainState.setEnabled(true);
                viewPort.attachScene(rootNode);
            }
        }
    }
    
    //this is the callable that contains the code that is run on the other thread.
    //since the assetmananger is threadsafe, it can be used to load data from any thread
    //we do *not* attach the objects to the rootNode here!
    Callable<Void> loadingCallable = new Callable<Void>() {
 
        @Override
        public Void call() {
            Element element = nifty.getScreen("loadlevel").findElementByName("loadingtext");
            textRenderer = element.getRenderer(TextRenderer.class);
 
            setProgress(0.0f, "Loading city model");
 
            flyCam.setMoveSpeed(MainState.FLYCAM_SPEED);
            flyCam.setDragToRotate(false);
            flyCam.registerWithInput(inputManager);       
        
            hud = new HUD(settings);
        
            //rootNode.attachChild(SkyFactory.createSky(app.getAssetManager(), "Scenes/Beach/FullskiesSunset0068.dds", false));
        
            BlenderKey blenderKey = new BlenderKey("City.blend");
            blenderKey.setLoadObjectProperties(true);
        
            long timer = System.currentTimeMillis();
            rawWorld = (Node)assetManager.loadAsset(blenderKey);
            //rawWorld = (Node) assetManager.loadModel("City.j3o");
            logger.log(Level.INFO, "City finished loading in {0} ms", System.currentTimeMillis() - timer);
        
            bulletAppState = new BulletAppState();
            stateManager.attach(bulletAppState);
            if (CityApplication.DEBUG) {
                bulletAppState.getPhysicsSpace().enableDebug(assetManager);
            }
        
            rawWorld.setName("World");
            rawWorld.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

            vehicles = new Node("Vehicles");
            rootNode.attachChild(vehicles);

            //Logger.getLogger("").setLevel(Level.SEVERE);
        
            setProgress(0.25f, "Building nodes");
            
            initWorld();
            
            rootNode.attachChild(rawWorld);
            al = new PointLight();
            al.setColor(ColorRGBA.White.mult(.1f));
            al.setPosition(new Vector3f(0, 12, 8));
            
            setProgress(0.4f, "Setting up physics");
        
            CollisionShape level_shape = CollisionShapeFactory.createMeshShape(rawWorld);

            RigidBodyControl cityControl = new RigidBodyControl(level_shape, 0);
            rawWorld.addControl(cityControl);
            bulletAppState.getPhysicsSpace().add(cityControl);
            
            setProgress(0.6f, "Setting up shadows and lights");

            rootNode.attachChild(SkyFactory.createSky(
            assetManager, "Textures/Sky/Bright/BrightSky.dds", false));

            rootNode.setShadowMode(RenderQueue.ShadowMode.Off);
            initSun();
            cam.setFrustumFar(5000);
            cam.setLocation(new Vector3f(90, 10, -112f));
            
            setProgress(0.7f, "Building AI");
            
            testAI = new AIVehicle(rootNode, world, new Vector3f(-107, 0, 95f), new Vector3f(0, FastMath.PI, 0), new Vector3f(1.79578f, 1.47574f, 4.5974f), inputManager, viewPort, assetManager, vehicles, bulletAppState.getPhysicsSpace());
            cam.lookAt(testAI.getVehicle().getPlayer().getPhysicsLocation(), Vector3f.UNIT_Y);
            testAI.reorient();
            testAI.generateRandomDestination();

            if (CityApplication.DEBUG) {
                bulletAppState.getPhysicsSpace().enableDebug(assetManager);    
            }
                    
            setProgress(1f, "Done loading city");
 
            return null;
        }
    };
    
    private void initWorld() {
        streetInfoMap = new HashMap<>();
        intersectionInfoMap = new HashMap<>();
        sensors = new ArrayList<>();
        trafficSignalList = new ArrayList<>();
        
        //if (!CityApplication.DEBUG) {
            adjustPhysics();
        //}
        
        world = NodeInspector.buildNode(rootNode, rawWorld, streetInfoMap, intersectionInfoMap, sensors);
        definedSignalsControlList = DefinedSignalControlUtility.addControls("trafficSignalControls.txt",(Node)world.getChild("Traffic Signals"));
        signalizedIntersectionList = SignalizedIntersectionUtility.buildIntersections(definedSignalsControlList, (Node)world.getChild("Traffic Signals"));
        
        for(SignalControl s: definedSignalsControlList) {
            s.begin(rawWorld);
        }
        
        StreetlightUtility.initStreetlights((Node)world.getChild("Streetlights"));

        //System.out.println("Num of intersections: " + signalizedIntersectionList.size());    
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
    
    public void setProgress(final float progress, final String loadingText) {
        //since this method is called from another thread, we enqueue the changes to the progressbar to the update loop thread
        enqueue(new Callable() {
            @Override
            public Object call() throws Exception {
                final int MIN_WIDTH = 32;
                int pixelWidth = (int) (MIN_WIDTH + (progressBarElement.getParent().getWidth() - MIN_WIDTH) * progress);
                progressBarElement.setConstraintWidth(new SizeValue(pixelWidth + "px"));
                progressBarElement.getParent().layoutElements();

                textRenderer.setText(loadingText);
                return null;
            }
        });
        
        
    }
    
    public void showLoadingMenu() {
        nifty.gotoScreen("loadlevel");
        load = true;
    }
    
    @Override
    public void bind(Nifty nifty, Screen screen) {
        progressBarElement = nifty.getScreen("loadlevel").findElementByName("progressbar");
    }
    
    @Override
    public void onStartScreen() { }
    
    @Override
    public void onEndScreen() { }
    
    // methods for Controller
    @Override
    public boolean inputEvent(final NiftyInputEvent inputEvent) {
        return false;
    }
    
    @Override
    public void onFocus(boolean getFocus) { }
    
    @Override
    public void init(Properties prprts, Attributes atrbts) { }
    
    @Override
    public void bind(Nifty nifty, Screen screen, Element elmnt, Properties prprts, Attributes atrbts) {
        progressBarElement = elmnt.findElementByName("progressbar");
    }
    
    public MainState getMainState() {
        return mainState;
    }
    
    public PauseState getPauseState() {
        return pauseState;
    }
    
    public ScheduledThreadPoolExecutor getExecutor() {
        return executor;
    }
    
    public HUD getHUD() {
        return hud;
    }
    
    public Node getRawWorldNode() {
        return rawWorld;
    }
    
    public BulletAppState getBulletAppState() {
        return bulletAppState;
    }
    
    public PointLight getAl() {
        return al;
    }
    
    public AIVehicle getTestAIVehicle() {
        return testAI;
    }
    
    public Vector3f getLightDir() {
        return lightDir;
    }

    public DirectionalLight getSun() {
        return sun;
    }

    public HashMap<String, StreetInfo> getStreetInfoMap() {
        return streetInfoMap;
    }

    public HashMap<String, IntersectionInfo> getIntersectionInfoMap() {
        return intersectionInfoMap;
    }
    
    public ArrayList<Sensor> getSensors() {
        return sensors;
    }

    public ArrayList<Intersection> getTrafficSignalList() {
        return trafficSignalList;
    }

    public ArrayList<DefinedSignalControl> getDefinedSignalsControlList() {
        return definedSignalsControlList;
    }

    public ArrayList<SignalizedIntersection> getSignalizedIntersectionList() {
        return signalizedIntersectionList;
    }
    
    public Node getVehiclesNode() {
        return vehicles;
    }
}