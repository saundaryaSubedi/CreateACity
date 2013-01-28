package createacity.ai;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.cinematic.MotionPath;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import createacity.CityHelper;
import createacity.Vehicle;


public class AIVehicle {
    private Vehicle vehicle;
    private int currentRoadNumberOfLanes;
    private int currentRoadLane;
    private int currentRoadLaneDesired;
    private float currentSpeedLimit;
    private Node rootNode, worldNode, currentRoadNode;
    private float steeringSensitivity;
    LinkedList<MotionPath> motionPaths;
    MotionPath currentMotionPath;
    private AssetManager assetManager;
    Vector3f normalForwardVector = new Vector3f(0, 0, -1);
    private boolean needsReorienting = false;
    //private Queue drivingInstructions;
    
    public AIVehicle(Node rootNode, Node worldNode, Vector3f location, Vector3f rotation, Vector3f dimensions, InputManager inputManager, ViewPort viewPort, AssetManager assetManager, Node parentNode, PhysicsSpace pSpace) {
        vehicle = new Vehicle("AI", "Sedan", location, rotation, dimensions, 0.2286f, inputManager, viewPort, assetManager, parentNode, pSpace);
        vehicle.getCombustionPropUnit().getShifter().setGear("DRIVE");
        this.rootNode = rootNode;
        this.worldNode = worldNode;
        motionPaths = new LinkedList<MotionPath>();
        MotionPath path = new MotionPath();
        path.addWayPoint(new Vector3f(100, 0, -112));
        path.addWayPoint(new Vector3f(105, 0, -106));
        path.addWayPoint(new Vector3f(108, 0, -98f));
        path.enableDebugShape(assetManager, rootNode);
        MotionPath path2 = new MotionPath();
        path2.addWayPoint(new Vector3f(-108, 0, -98f));
        path2.addWayPoint(new Vector3f(-107, 0, -110));
        path2.addWayPoint(new Vector3f(-97, 0, -112));
        path2.enableDebugShape(assetManager, rootNode);
        this.assetManager = assetManager;
        
        //path.addWayPoint(new Vector3f(110, 0, 0));
        path.setCurveTension(.5f);
        path2.setCurveTension(.5f);
        motionPaths.offer(path);
        motionPaths.offerFirst(path2);
        needsReorienting = true;
    }
    
    public void step(float tpf) {
        while(needsReorienting) {
            reorient();
        }
        
        if (vehicle.getCombustionPropUnit().getSpeed() < currentSpeedLimit) {
            vehicle.getCombustionPropUnit().activate(1.0f, true);
        }
        else {
            vehicle.getCombustionPropUnit().activate(0.0f, false);
        }
        
        
        
        checkPath(tpf);
        
        //System.out.println("Steer value: " + vehicle.getSteeringValue());
        vehicle.update(tpf);
    }
    
    public void generateRandomDestination() {
        System.out.println("Generating random destination...");
        Node roadsNode = (Node)worldNode.getChild("Roads");
        Random randRoad = new Random();
        Node randRoadParentNode, randRoadName;
        do {
            randRoadParentNode = ((Node)roadsNode.getChild(randRoad.nextInt(roadsNode.getChildren().size())));
        } while(randRoadParentNode.getChildren().isEmpty());
        
        do {
            randRoadName = ((Node)randRoadParentNode.getChild(randRoad.nextInt(randRoadParentNode.getChildren().size())));
        } while(randRoadName.getChildren().isEmpty());
        
        Node randRoadFinal = (Node)randRoadName.getChild(randRoadName.getChildren().size() - 1);
        float endAddress = Float.parseFloat((String)randRoadFinal.getUserData("addressEnd"));
        int randAddress = randRoad.nextInt((int)endAddress) + 1;
        
        System.out.println("Destination: " + randAddress + " " + (String)randRoadFinal.getUserData("roadName"));
    }
    
    private void checkPath(float tpf) {
        //System.err.println("Reorienting is " + needsReorienting);
        if (motionPaths.size() > 0)
            checkIfOnPath();
        
        //System.out.println("currentMotionPath is " + currentMotionPath);
        
        if (currentMotionPath == null)
            stayInMiddleOfCurrentLane(tpf);
        else {
            if (vehicle.getPlayer().getPhysicsLocation().distance(currentMotionPath.getWayPoint(0)) < 1f) {
                currentMotionPath.removeWayPoint(0);
                System.out.println("Removing waypoint");
                
                if (currentMotionPath.getNbWayPoints() == 0) {
                    currentMotionPath = null;
                    needsReorienting = true;
                }
                
            } else {
                //System.out.println("Desired path: " + currentMotionPath.getWayPoint(0));
                Vector3f path = currentMotionPath.getWayPoint(0).subtract(vehicle.getPlayer().getPhysicsLocation());
                Vector3f forward = vehicle.getPlayer().getForwardVector(null);
                forward.normalizeLocal();
                path.normalizeLocal();
            
                Vector3f dir = path.subtract(forward);
                steeringSensitivity = 1.5f;
                //System.out.println(dir);

                if (FastMath.abs(dir.getZ()) < .1f) {
                    //if (steeringSensitivity + .01f >= .01f)
                    //steeringSensitivity += .1f;
                    //vehicle.setSteeringValue(0);
                } else if (dir.getZ() < 0) {
                    //System.out.println("Steer left");
                    //vehicle.setSteeringValue(vehicle.getSteeringValue() + (tpf / 100f) * FastMath.abs(dir.getZ() * 10f));
                    vehicle.setSteeringValue(-steeringSensitivity * dir.getZ());
                } else if (dir.getZ() > 0) {
                    //System.out.println("Steer right");
                    //vehicle.setSteeringValue(vehicle.getSteeringValue() - (tpf / 100f) * FastMath.abs(dir.getZ() * 10f));
                    vehicle.setSteeringValue(-steeringSensitivity * dir.getZ());
                }
            }
            
            
        }
    }
    
    private void checkIfOnPath() {
        
        if (vehicle.getPlayer().getPhysicsLocation().distance(motionPaths.peek().getWayPoint(0)) < 1f) {
            //System.exit(0);
            currentMotionPath = motionPaths.poll();
        }
    }
    
    //Stay in middle of current lane
    private void stayInMiddleOfCurrentLane(float tpf) {
        Vector3f leftNormal = Vector3f.ZERO, rightNormal = Vector3f.ZERO, middleOfCurrentLane = Vector3f.ZERO;
        Quaternion rotateLeft = new Quaternion(), rotateRight = new Quaternion();
        rotateLeft.fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
        rotateRight.fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y);
        
        
        Ray leftRay, rightRay;
        
        CollisionResults leftResults = new CollisionResults();
        CollisionResults rightResults = new CollisionResults();
        //Vector3f bottom = new Vector3f(vehicle.getPlayer().getPhysicsLocation().getX(), vehicle.getPlayer().getPhysicsLocation().getY() - vehicle.getPlayer().getCollisionShape().getScale().getY(), vehicle.getPlayer().getPhysicsLocation().getZ());
        Vector3f direction = new Vector3f();
        vehicle.getPlayer().getPhysicsRotation().getRotationColumn(0, direction);
        vehicle.getPlayer().getPhysicsRotation().getRotationColumn(1, direction);
        vehicle.getPlayer().getPhysicsRotation().getRotationColumn(2, direction);
        //direction.normalizeLocal();
        Vector3f directionRight = rotateRight.mult(direction);
        //directionRight.normalizeLocal();
        Vector3f directionLeft = rotateLeft.mult(direction);
        //directionLeft.normalizeLocal();
        
        //System.out.println("LEFT: " + directionLeft.normalizeLocal());
        //System.out.println("RIGHT: " + directionRight.normalizeLocal());
        
        leftRay = new Ray(vehicle.getPlayer().getPhysicsLocation().add(0, .25f, 0), directionLeft);
        rightRay = new Ray(vehicle.getPlayer().getPhysicsLocation().add(0, .25f, 0), directionRight);
 
        ((Node)rootNode.getChild("World")).collideWith(leftRay, leftResults);
        ((Node)rootNode.getChild("World")).collideWith(rightRay, rightResults);
        
        if (currentRoadLane == 0) {
            
        } else if (currentRoadLane < currentRoadNumberOfLanes) {
            //System.out.println("Steering value: " + vehicle.getSteeringValue());
            //System.exit(0);
            if (leftResults.size() > 0 && leftResults.getClosestCollision().getDistance() < 10){
                Iterator<CollisionResult> itr = leftResults.iterator();
                
                while(itr.hasNext()) {
                    CollisionResult currentResult = itr.next();
                    
                    if (currentResult.getGeometry().getParent().getName().equals("RL_" + currentRoadNode.getName().substring(2) + "_" + (currentRoadLane - 1))) {
                        leftNormal = currentResult.getContactPoint();
                    }
                    
                    if (currentResult.getGeometry().getParent().getName().equals("RL_" + currentRoadNode.getName().substring(2) + "_" + (currentRoadLane))) {
                        rightNormal = currentResult.getContactPoint();
                    }
                }
                
                //System.out.println(leftResults.g);
            }

            if (rightResults.size() > 0 && rightResults.getClosestCollision().getDistance() < 10){
                Iterator<CollisionResult> itr = rightResults.iterator();
                
                while(itr.hasNext()) {
                    CollisionResult currentResult = itr.next();
                    
                    if (currentResult.getGeometry().getParent().getName().equals("RL_" + currentRoadNode.getName().substring(2) + "_" + (currentRoadLane - 1))) {
                        leftNormal = currentResult.getContactPoint();
                    }
                    
                    if (currentResult.getGeometry().getParent().getName().equals("RL_" + currentRoadNode.getName().substring(2) + "_" + (currentRoadLane))) {
                        rightNormal = currentResult.getContactPoint();
                    }
                }
            }
            
            middleOfCurrentLane = leftNormal.add(rightNormal).divide(2f);
            Vector3f forwardVector = vehicle.getPlayer().getForwardVector(null);
            Vector3f laneVector = middleOfCurrentLane.subtract(vehicle.getPlayer().getPhysicsLocation());
            //forwardVector.normalizeLocal();
            //laneVector.normalizeLocal();
            
            
            Vector3f cross = forwardVector.cross(laneVector);
            //System.out.println(currentRoadNode.getName() + " " + cross.getY());
            steeringSensitivity = FastMath.abs(cross.getY());
            if (FastMath.abs(cross.getY()) < .1f) {
                //if (steeringSensitivity + .01f >= .01f)
                //steeringSensitivity += .1f;
                vehicle.setSteeringValue(0);
            } else if (cross.getY() > 0) {
                //System.out.println("Steer left");
                //vehicle.setSteeringValue(vehicle.getSteeringValue() + (tpf / 100f) * FastMath.abs(dir.getZ() * 10f));
                vehicle.setSteeringValue(steeringSensitivity * cross.getY());
            } else if (cross.getY() < 0) {
                //System.out.println("Steer right");
                //vehicle.setSteeringValue(vehicle.getSteeringValue() - (tpf / 100f) * FastMath.abs(dir.getZ() * 10f));
                vehicle.setSteeringValue(steeringSensitivity * cross.getY());
            }
        } else {
            
        }
            //System.out.println(rightNormal.distance(vehicle.getPlayer().getPhysicsLocation()));
                //System.exit(0);
        //System.out.println(leftNormal.distance(vehicle.getPlayer().getPhysicsLocation()) + " " + rightNormal.distance(vehicle.getPlayer().getPhysicsLocation()));
        //System.out.println(vehicle.getSteeringValue());
    }
    
    public void reorient() {
        System.out.println("Reorienting...");
        steeringSensitivity = .05f;
        vehicle.setSteeringValue(0);
        currentRoadNode = getCurrentRoadNode();
        System.out.println("Current road node: " + currentRoadNode.getName());
        if (currentRoadNode != null && currentRoadNode.getChildren().size() > 0) {
            int c = currentRoadNode.getChildren().size() - 1;
            
            while (!currentRoadNode.getChild(c).getName().startsWith("RL_")) {
                c--;
            }
            currentRoadNumberOfLanes = 2 + Integer.parseInt(currentRoadNode.getChild(c).getName().substring(currentRoadNode.getChild(c).getName().length()-1));
            currentRoadLane = determineCurrentRoadLane();           
            currentSpeedLimit = getCurrentRoadSpeedLimit();
            System.out.println("Current road: " + currentRoadNode.getName());
            System.out.println("Number of lanes: " + currentRoadNumberOfLanes);
            System.out.println("Current lane: " + currentRoadLane);
            System.out.println("Speed limit: " + currentSpeedLimit);
            System.out.println("Front location: " + getFrontVehicleLoc());
        }
        
        needsReorienting = false;
    }
    
    private int determineCurrentRoadLane() {
        int currentLane = -1;
        String leftLaneName = "", rightLaneName = "";
        Quaternion rotateLeft = new Quaternion(), rotateRight = new Quaternion();
        rotateLeft.fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
        rotateRight.fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y);
        
        
        Ray leftRay, rightRay;
        
        CollisionResults leftResults = new CollisionResults();
        CollisionResults rightResults = new CollisionResults();
        //Vector3f bottom = new Vector3f(vehicle.getPlayer().getPhysicsLocation().getX(), vehicle.getPlayer().getPhysicsLocation().getY() - vehicle.getPlayer().getCollisionShape().getScale().getY(), vehicle.getPlayer().getPhysicsLocation().getZ());
        Vector3f direction = new Vector3f();
        vehicle.getPlayer().getPhysicsRotation().getRotationColumn(0, direction);
        vehicle.getPlayer().getPhysicsRotation().getRotationColumn(1, direction);
        vehicle.getPlayer().getPhysicsRotation().getRotationColumn(2, direction);
        //direction.normalizeLocal();
        Vector3f directionRight = rotateRight.mult(direction);
        //directionRight.normalizeLocal();
        Vector3f directionLeft = rotateLeft.mult(direction);
        //directionLeft.normalizeLocal();
        
        //System.out.println("LEFT: " + directionLeft.normalizeLocal());
        //System.out.println("RIGHT: " + directionRight.normalizeLocal());
        
        leftRay = new Ray(vehicle.getPlayer().getPhysicsLocation().add(0, .25f, 0), directionLeft);
        rightRay = new Ray(vehicle.getPlayer().getPhysicsLocation().add(0, .25f, 0), directionRight);
 
        ((Node)rootNode.getChild("World")).collideWith(leftRay, leftResults);
        ((Node)rootNode.getChild("World")).collideWith(rightRay, rightResults);

        if (leftResults.size() > 0 && leftResults.getClosestCollision().getDistance() < 10){
            leftLaneName = leftResults.getClosestCollision().getGeometry().getParent().getName();
            //System.out.println("Listing left collisions...");
            for(int i = 0; i < leftResults.size(); i++) {
                //System.out.println(leftResults.getCollision(i).getGeometry().getParent().getName());
            }
        }
        
        if (rightResults.size() > 0 && rightResults.getClosestCollision().getDistance() < 10){
            rightLaneName = rightResults.getClosestCollision().getGeometry().getParent().getName();
            //System.out.println("Listing right collisions...");
            for(int i = 0; i < rightResults.size(); i++) {
                //System.out.println(rightResults.getCollision(i).getGeometry().getParent().getName());
            }
        }
        
        //System.out.println("L" + leftLaneName + " R" + rightLaneName);
        
        if (!leftLaneName.startsWith("RL_")) { //Leftmost lane
            currentLane = 0;
        } else if (!rightLaneName.startsWith("RL_")) { //Rightmost lane
            currentLane = Integer.parseInt(leftLaneName.substring(leftLaneName.length()-1)) + 1;
        } else {
            currentLane = Integer.parseInt(rightLaneName.substring(rightLaneName.length()-1));
        }
        
        return currentLane;
    }
    
    //Gets spatial name of current road
    public String getCurrentRoad() {
        String s = "Could not determine current road";
        
        Ray ray;
        
        CollisionResults results = new CollisionResults();

        ray = new Ray(vehicle.getPlayer().getPhysicsLocation(), new Vector3f(0, -1, 0));
 
        ((Node)rootNode.getChild("World")).collideWith(ray, results);

        if (results.size() > 0 && results.getClosestCollision().getDistance() < 10){
            s = results.getClosestCollision().getGeometry().getParent().getName();
        }
        
        
        return s;
    }
    
    public Node getCurrentRoadNode() {
        Node roadsNode = (Node)worldNode.getChild("Roads");
        String currentRoad = getCurrentRoad();
        Node parentNode = new Node();
        currentRoadNode = new Node();
        
        char start = currentRoad.charAt(2);
        start = Character.toUpperCase(start);
        if (Character.isDigit(start)){
            parentNode = (Node)roadsNode.getChild((int) start);
        }
        else if (Character.getType(start) == Character.UPPERCASE_LETTER){                  
            parentNode = (Node)roadsNode.getChild(((int) start) - 55);
        }
        else
            throw new IllegalArgumentException(currentRoad.substring(2) + " does not start with a letter or number!");
        
        //Determine number of lanes on current road
        for (int a = 0; a < parentNode.getChildren().size(); a++) { 
                for (int b = 0; b < ((Node) parentNode.getChild(a)).getChildren().size(); b++) {
                    if ((((Node) parentNode.getChild(a)).getChild(b).getName().equals(currentRoad))) {
                        return (Node)((Node) parentNode.getChild(a)).getChild(b);
                    }
                }
        }
        
        return currentRoadNode;
    }
    
    private float getCurrentRoadSpeedLimit() {
        return CityHelper.kphtoms(Float.parseFloat((String)currentRoadNode.getUserData("speedLimit")));
    }
    
    public Vector3f getFrontVehicleLoc() {
        Vector3f frontLoc = vehicle.getPlayer().getPhysicsLocation();
        Vector3f toFront = vehicle.getPlayer().getForwardVector(null).normalizeLocal().mult(vehicle.getDimensions().getZ() * 0.5f);
        
        
        return frontLoc.add(toFront);
    }
    
    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
}