package createacity;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.HullCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.light.Light;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.UserData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;
import createacity.states.MainState;

/**
 * A utility class for the creation, modification, and debugging of spatials (Nodes and Geometries)
 */
public class NodeInspector {
    /** 
     * Builds a node from a .blend file
     * @param rootNode the root node
     * @param n node from Blender file
     * @param 
     * @return node to use internally
     */
    public static Node buildNode(Node rootNode, Node n, HashMap<String, StreetInfo> streetInfoMap, HashMap<String, IntersectionInfo> intersectionInfoMap,
                                 ArrayList<Sensor> sensors){
        Node node = new Node(n.getName());
        String lastSensorName = "";
        ArrayList<Vector3f[]> sensorInfo = new ArrayList<Vector3f[]>();
        
        //Add lights
        for (Light l: n.getLocalLightList()) {
            rootNode.getLocalLightList().add(l);
        }
        
        
        Node streetlights = new Node("Streetlights"); 
        Node roads = new Node("Roads");
        Node intersections = new Node("Intersections");
        Node trafficSignals = new Node("Traffic Signals");
        
        //Process roads
        
        //Add "0" through "9"
        for(int i = 0; i <= 9; i++) {
            roads.attachChild(new Node(String.valueOf(i)));
        }
        
        //Add "A" through "Z"
        for(int i = (int)'A'; i <= (int)'Z'; i++) {
            roads.attachChild(new Node(String.valueOf((char)i)));
        }
        
        //Add "0" through "9"
        for(int i = 0; i <= 9; i++) {
            intersections.attachChild(new Node(String.valueOf(i)));
        }
        
        //Add "A" through "Z"
        for(int i = (int)'A'; i <= (int)'Z'; i++) {
            intersections.attachChild(new Node(String.valueOf((char)i)));
        } 
        
        for(int i = 0; i < n.getChildren().size(); i++){
            
            System.out.println("Processing " + n.getChild(i).getName());
            //Roads
            if (n.getChild(i).getName().startsWith("R_")){
                
                Node parentNode = new Node();
                char start = n.getChild(i).getName().charAt(2);
                start = Character.toUpperCase(start);
                if (Character.isDigit(start)){
                    System.out.println("Starts with a digit");
                    parentNode = (Node)roads.getChild((int) start);
                }
                else if (Character.getType(start) == Character.UPPERCASE_LETTER){                  
                    parentNode = (Node)roads.getChild(((int) start) - 55);
                }
                else {
                    throw new IllegalArgumentException(n.getChild(i).getName().substring(2) + " does not start with a letter or number!");
                }
                System.out.println("Road's parent node is: " + parentNode.getName());

                if (!n.getChild(i).getUserDataKeys().isEmpty()) {
                    int loc = getIndexOfMatchingName(parentNode, n.getChild(i).getUserData("roadName") + "_" + n.getChild(i).getUserData("borough"));
                    if (loc == -1){
                        Node streetNode = new Node(n.getChild(i).getUserData("roadName") + "_" + n.getChild(i).getUserData("borough"));
                        copySpatial(streetNode, n.getChild(i));
                        copySpatial(parentNode, streetNode);

                    } else{
                        copySpatial((Node)parentNode.getChild(loc), n.getChild(i));
                    }
                    
                    addRoadInfo((Node)n.getChild(i), streetInfoMap);
                }
                
                
                //if (((Node)roads.getChild(n.getChild(i).getName().substring(2,3).toUpperCase())).hasChild(roads))
            }
            
            //Lanes
            if (n.getChild(i).getName().startsWith("RL_")){
                
                int end = n.getChild(i).getName().length() - 2;
                
                Node parentNode = new Node();
                char start = n.getChild(i).getName().charAt(3);
                start = Character.toUpperCase(start);
                if (Character.isDigit(start)){
                    parentNode = (Node)roads.getChild((int) start);
                }
                else if (Character.getType(start) == Character.UPPERCASE_LETTER){                  
                    parentNode = (Node)roads.getChild(((int) start) - 55);
                }
                else {
                    throw new IllegalArgumentException(n.getChild(i).getName().substring(2) + " does not start with a letter or number!");
                }
                
                for (int a = 0; a < parentNode.getChildren().size(); a++) {
                    
                    for (int b = 0; b < ((Node) parentNode.getChild(a)).getChildren().size(); b++) {
                        if ((((Node) parentNode.getChild(a)).getChild(b).getName().
                                substring(2).equals(n.getChild(i).getName().substring(3,end)))) {
                            copySpatial((Node)((Node) parentNode.getChild(a)).getChild(b), n.getChild(i));
                        }
                    }
                }

                /*if (!n.getChild(i).getUserDataKeys().isEmpty()) {
                    int loc = getIndexOfMatchingName(parentNode, n.getChild(i).getUserData("roadName") + "_" + n.getChild(i).getUserData("borough"));
                    if (loc == -1){
                        Node streetNode = new Node(n.getChild(i).getUserData("roadName") + "_" + n.getChild(i).getUserData("borough"));
                        copySpatial(streetNode, n.getChild(i));
                        copySpatial(parentNode, streetNode);

                    } else{
                        copySpatial((Node)parentNode.getChild(loc), n.getChild(i));
                    }
                    
                }*/
            }
            
            //Intersections
            if (n.getChild(i).getName().startsWith("X_")){
                createIntersection((Node)n.getChild(i), intersectionInfoMap);
            }
            
            //Process streetlights
            if (n.getChild(i).getName().startsWith("SL_")){
                copySpatial(streetlights, n.getChild(i));
            }
            
            //Process sensors
            if (n.getChild(i).getName().startsWith("SENSOR_")) {
                String sensorRawName = n.getChild(i).getName();
                String sensorName = sensorRawName.substring(0, sensorRawName.lastIndexOf("_"));
                float[] sensorAngles = n.getChild(i).getWorldRotation().toAngles(null);
                Vector3f sensorLocation = n.getChild(i).getWorldTranslation();
                Vector3f sensorDirection = new Vector3f(sensorAngles[0], sensorAngles[1], sensorAngles[2]);
                sensorDirection.normalizeLocal();
                System.out.println("Sensor name: " + sensorName);
                System.out.println("Sensor rotation: " + "(" + sensorAngles[0] + "," + sensorAngles[1] + "," + sensorAngles[2] + ")");
                Vector3f[] thisSensor = new Vector3f[2];
                thisSensor[0] = sensorLocation;
                thisSensor[1] = sensorDirection;
                if (lastSensorName.isEmpty() || !sensorName.equals(lastSensorName)) {
                    if (!lastSensorName.isEmpty()) {
                        //Create a sensor with the info
                        Vector3f[][] thisSensorInfo = new Vector3f[sensorInfo.size()][2];
                        sensorInfo.toArray(thisSensorInfo);
                        sensors.add(new VehicleSensor(lastSensorName, thisSensorInfo));

                        //Clear the sensor info to make room for a new sensor
                        sensorInfo.clear();
                    }
                    lastSensorName = sensorName; 
                }
                sensorInfo.add(thisSensor);
                //sensorName.
            }
            
            //Process traffic signals and lights
            if (n.getChild(i).getName().startsWith("SIGNAL_")) {
                String nodeName = n.getChild(i).getName(), intersectionName;
                int current;
                current = nodeName.indexOf("_") + 1;
                while(!Character.isDigit(nodeName.charAt(current))) {
                    current++;
                }
                current++;
                intersectionName = nodeName.substring(0, current);
                if (trafficSignals.getChild(intersectionName) == null) {
                    trafficSignals.attachChild(new Node(intersectionName));
                }
                
                copySpatial((Node)trafficSignals.getChild(intersectionName), n.getChild(i));
                
                for (int j = 0; j < ((Node)(n.getChild(i))).getChildren().size(); j++) {
                    if (((Node)(n.getChild(i))).getChild(j).getName().startsWith("SIGNALLIGHT_")) {
                       ((Node)(n.getChild(i))).detachChildAt(j);
                       j--;
                    }
                }
            }
            
             System.out.println("*");
        }
        
       
        System.out.println("Outside of for loop");
        if (!lastSensorName.isEmpty()) {
            //Add last sensor
            Vector3f[][] thisSensorInfo = new Vector3f[sensorInfo.size()][2];
            sensorInfo.toArray(thisSensorInfo);
            sensors.add(new VehicleSensor(lastSensorName, thisSensorInfo));
        }
        
        node.attachChild(streetlights);
        
        
        System.out.println("Sensors in NodeInspector: " + sensors);
      
        
        node.attachChild(roads);
        node.attachChild(trafficSignals);
        
        return node;
    }
    
    public static CompoundCollisionShape createCompoundShape(Node realRootNode,
            Node rootNode, CompoundCollisionShape shape, boolean meshAccurate, boolean dynamic) {
        for (Spatial spatial : rootNode.getChildren()) {
            if (spatial instanceof Node) {
                createCompoundShape(realRootNode, (Node) spatial, shape, meshAccurate, dynamic);
            } else if (spatial instanceof Geometry) {
                System.out.println(spatial.getName() + " " + spatial.getUserData(UserData.JME_PHYSICSIGNORE));
                Boolean bool = spatial.getUserData(UserData.JME_PHYSICSIGNORE);
                if (bool != null && bool.booleanValue()) {
                    continue; // go to the next child in the loop
                }
                if (meshAccurate) {
                    CollisionShape childShape = dynamic
                            ? createSingleDynamicMeshShape((Geometry) spatial, realRootNode)
                            : createSingleMeshShape((Geometry) spatial, realRootNode);
                    if (childShape != null) {
                        Transform trans = getTransform(spatial, realRootNode);
                        shape.addChildShape(childShape,
                                trans.getTranslation(),
                                trans.getRotation().toRotationMatrix());
                    }
                } 
            }
        }
        return shape;
    }
    
    /**
     * This type of collision shape is mesh-accurate and meant for immovable "world objects".
     * Examples include terrain, houses or whole shooter levels.<br>
     * Objects with "mesh" type collision shape will not collide with each other.
     */
    private static MeshCollisionShape createSingleMeshShape(Geometry geom, Spatial parent) {
        Mesh mesh = geom.getMesh();
        Transform trans = getTransform(geom, parent);
        if (mesh != null) {
            MeshCollisionShape mColl = new MeshCollisionShape(mesh);
            mColl.setScale(trans.getScale());
            return mColl;
        } else {
            return null;
        }
    }
    
    /**
     * This method creates a hull collision shape for the given mesh.<br>
     */
    private static HullCollisionShape createSingleDynamicMeshShape(Geometry geom, Spatial parent) {
        Mesh mesh = geom.getMesh();
        Transform trans = getTransform(geom, parent);
        if (mesh != null) {
            HullCollisionShape dynamicShape = new HullCollisionShape(mesh);
            dynamicShape.setScale(trans.getScale());
            return dynamicShape;
        } else {
            return null;
        }
    }
    
    /**
     * returns the correct transform for a collisionshape in relation
     * to the ancestor for which the collisionshape is generated
     * @param spat
     * @param parent
     * @return
     */
    private static Transform getTransform(Spatial spat, Spatial parent) {
        Transform shapeTransform = new Transform();
        Spatial parentNode = spat.getParent() != null ? spat.getParent() : spat;
        Spatial currentSpatial = spat;
        //if we have parents combine their transforms
        while (parentNode != null) {
            if (parent == currentSpatial) {
                //real parent -> only apply scale, not transform
                Transform trans = new Transform();
                trans.setScale(currentSpatial.getLocalScale());
                shapeTransform.combineWithParent(trans);
                parentNode = null;
            } else {
                shapeTransform.combineWithParent(currentSpatial.getLocalTransform());
                parentNode = currentSpatial.getParent();
                currentSpatial = parentNode;
            }
        }
        return shapeTransform;
    }
    
    private static void addRoadInfo(Node roadNode, HashMap<String, StreetInfo> streetInfoMap){

        
     
        String next = roadNode.getUserData("nextRoad");
        String prev = roadNode.getUserData("prevRoad");
        streetInfoMap.put(roadNode.getName(), new StreetInfo((String)roadNode.getUserData("roadName"),next,prev));
                    
             
               

        
    }
    
    private static void createIntersection(Node intersectionNode, HashMap<String, IntersectionInfo> intersectionInfoMap) {
        LinkedHashMap<String, Integer[]> roadPointMap = new LinkedHashMap<String, Integer[]>();
        
        for(int i = 1; intersectionNode.getUserData("road" + i) != null; i++) {
            ArrayList<Integer> roadPts = new ArrayList<Integer>();
            
            Scanner locScanner = new Scanner((String)intersectionNode.getUserData("road" + i + "Loc"));
            while(locScanner.hasNextInt()) {
                roadPts.add(locScanner.nextInt());
            }
            
            
            
            
            
            Integer[] roadPtsArray = roadPts.toArray(new Integer[roadPts.size()]);
            roadPointMap.put((String)intersectionNode.getUserData("road" + i), roadPtsArray);
            
        }
        
        String intersectionName = intersectionNode.getName().substring(2);
        
        intersectionInfoMap.put(intersectionName, new IntersectionInfo(roadPointMap));
    }
    
    public static String getFullRoadName(Node rootNode, HashMap<String, StreetInfo> streetInfoMap, String roadName) {   
        Node roadNode = (Node)rootNode.getChild(MainState.ROADS_INDEX);
        Node firstNode = (Node)roadNode.getChild(Character.toString(Character.toUpperCase(roadName.charAt(0))));
        
        for (int i = 0; i < firstNode.getChildren().size(); i++) {
            for (int j = 0; j < ((Node)firstNode.getChild(i)).getChildren().size(); j++) {
                if (((Node)firstNode.getChild(i)).getChild(j).getName().substring(2).startsWith(roadName)) {
                    return streetInfoMap.get(((Node)firstNode.getChild(i)).getChild(j).getName()).getStreetName();
                }
            }
        }
        
        return "FULL_ROAD_NOT_FOUND";
        
    }
    
    /**
     * Return the index of a child node or geometry  that matches a given name . This does not 
     * check for equality, simply whether a node/geometry exists with the same name.
     * @param node the node to check
     * @param childName the name of the node or geometry to check
     * @return 
     */
    public static int getIndexOfMatchingName(Node node, String childName){      
        for(int i = 0; i < node.getChildren().size(); i++){
            if (node.getChild(i).getName().equals(childName)){
                return i;
            }
        }
        
        return -1;
    }
    
    /** 
     * Copies s and all of its children as a child of parentNode
     * @param parentNode the parent node of s and its children
     * @param s the spatial to copy
     */
    public static void copySpatial(Node parentNode, Spatial s){
        parentNode.attachChild(s.clone());
    }
    
    /** 
     * Prints out an organized listing of a given spatial, including all of its children
     * @param level amount of tabs for the given spatial (usually 0 if called explicitly)
     * @param n the spatial to inspect
     */
    public static void inspectNode(int level, Spatial n){
        for(int j = 1; j <= level; j++){
                    System.out.print("\t");             
        }
        System.out.print(n.getName());
        if (n.getClass().getName().equals("com.jme3.scene.Geometry") || (n.getClass().getName().equals("com.jme3.scene.Node") && ((Node)n).getChildren().isEmpty())){
            System.out.println();
        }
        else{ //n is a Node with children
            for(int i = 0; i < ((Node)n).getLocalLightList().size(); i++){
                System.out.print("\t\"" + ((Node)n).getLocalLightList().get(i).getName() + "\"");
            }
            System.out.println();
            
            for(int i = 0; i < ((Node)n).getChildren().size(); i++){
                    inspectNode(level+1, ((Node)n).getChild(i));
                    
                
            }          
        }                                
    }
}