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
import createacity.CityApplication;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import createacity.CityHelper;
import createacity.Vehicle;

/**
 * Contains behaviors for automatically controlled vehicles.
 * Note that AI vehicles' direction is pi/2 more than what is shown in blender.
 * So if a road direction is shown as 90 degrees in blender, it should
 * be 180 degrees here.
 * 
 * @author Joseph
 */
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
    int currentWaypointIndex;
    private AssetManager assetManager;
    Vector3f normalForwardVector = new Vector3f(0, 0, -1);
    private boolean needsReorienting = false;
    //private Queue drivingInstructions;
    
    public AIVehicle(Node rootNode, Node worldNode, Vector3f location, Vector3f rotation, Vector3f dimensions, InputManager inputManager, ViewPort viewPort, AssetManager assetManager, Node parentNode, PhysicsSpace pSpace) {
        vehicle = new Vehicle("AI", "Sedan", location, rotation, dimensions, 0.2286f, inputManager, viewPort, assetManager, parentNode, pSpace);
        vehicle.getCombustionPropUnit().getShifter().setGear("DRIVE");
        this.rootNode = rootNode;
        this.worldNode = worldNode;
        motionPaths = new LinkedList<>();
        MotionPath path = new MotionPath();
        path.addWayPoint(new Vector3f(-108, 0, -98f));
        path.addWayPoint(new Vector3f(-104, 0, -105));
        path.addWayPoint(new Vector3f(-97, 0, -106));
        
        
        MotionPath path2 = new MotionPath();
        path2.addWayPoint(new Vector3f(100, 0, -108));
        path2.addWayPoint(new Vector3f(106, 0, -103));
        path2.addWayPoint(new Vector3f(108, 0, -98f));
        
        MotionPath path3 = new MotionPath();
        path3.addWayPoint(new Vector3f(108, 0, 98f));
        path3.addWayPoint(new Vector3f(106, 0, 103));
        path3.addWayPoint(new Vector3f(97.5f, 0, 108));
        
        MotionPath path4 = new MotionPath();
        path4.addWayPoint(new Vector3f(-97.5f, 0, 108));
        path4.addWayPoint(new Vector3f(-106, 0, 103));
        path4.addWayPoint(new Vector3f(-108, 0, 98f));
        
        this.assetManager = assetManager;
        
        //path.addWayPoint(new Vector3f(110, 0, 0));
        path.setCurveTension(.5f);
        path2.setCurveTension(.5f);
        motionPaths.offer(path);
        motionPaths.offer(path2);
        motionPaths.offer(path3);
        motionPaths.offer(path4);
        
        if (CityApplication.DEBUG) {
            for (MotionPath p: motionPaths) {
                p.enableDebugShape(assetManager, rootNode);
            }
        }
        
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
        if (motionPaths.size() > 0) {
            checkIfOnPath();
        }
        
        //System.out.println("currentMotionPath is " + currentMotionPath);
        
        if (currentMotionPath == null) {
            stayInMiddleOfCurrentLane(tpf);
        }
        else {
            if (vehicle.getPlayer().getPhysicsLocation().distance(currentMotionPath.getWayPoint(currentWaypointIndex)) < 1f) {
                //currentMotionPath.removeWayPoint(0);
                currentWaypointIndex++;
                System.out.println("Removing waypoint");
                
                if (currentWaypointIndex >= currentMotionPath.getNbWayPoints()) {
                    System.out.println("Finished waypoints");
                    currentMotionPath = null;
                    needsReorienting = true;
                }
                
            } else {
                //System.out.println("Desired path: " + currentMotionPath.getWayPoint(0));
                Vector3f path = currentMotionPath.getWayPoint(currentWaypointIndex).subtract(vehicle.getPlayer().getPhysicsLocation());
                Vector3f forward = vehicle.getPlayer().getForwardVector(null);
                forward.normalizeLocal();
                path.normalizeLocal();
            
                Vector3f dir = path.subtract(forward);
                steeringSensitivity = 20f;
                //System.out.println(dir);

                if (FastMath.abs(dir.getZ()) < .1f) {
                    //if (steeringSensitivity + .01f >= .01f)
                    //steeringSensitivity += .1f;
                    //vehicle.setSteeringValue(0);
                } else if (dir.getZ() < 0) {
                    //System.out.println("Steer left");
                    //vehicle.setSteeringValue(vehicle.getSteeringValue() + (tpf / 100f) * FastMath.abs(dir.getZ() * 10f));
                    vehicle.setSteeringValue(steeringSensitivity * dir.getZ());
                } else if (dir.getZ() > 0) {
                    //System.out.println("Steer right");
                    //vehicle.setSteeringValue(vehicle.getSteeringValue() - (tpf / 100f) * FastMath.abs(dir.getZ() * 10f));
                    vehicle.setSteeringValue(-steeringSensitivity * dir.getZ());
                }
            }
            
            
        }
    }
    
    private void checkIfOnPath() {
        for(MotionPath p: motionPaths) {
            if (vehicle.getPlayer().getPhysicsLocation().distance(p.getWayPoint(0)) < 1f) {
                currentMotionPath = p;
                currentWaypointIndex = 0;
            }
        }
    }
    
    //Stay in middle of current lane
    private void stayInMiddleOfCurrentLane(float tpf) {
        Vector3f leftNormal = Vector3f.ZERO, rightNormal = Vector3f.ZERO, middleOfCurrentLane = Vector3f.ZERO;
        float[] leftDirection = null, rightDirection = null;
        Quaternion leftQ = null, rightQ = null;
        Quaternion rotateLeft = new Quaternion(), rotateRight = new Quaternion();
        rotateLeft.fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
        rotateRight.fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y);
        
        
        Ray leftRay, rightRay;
        
        CollisionResults leftResults = new CollisionResults();
        CollisionResults rightResults = new CollisionResults();
        
        Vector3f direction = new Vector3f();
        vehicle.getPlayer().getPhysicsRotation().getRotationColumn(0, direction);
        vehicle.getPlayer().getPhysicsRotation().getRotationColumn(1, direction);
        vehicle.getPlayer().getPhysicsRotation().getRotationColumn(2, direction);

        Vector3f directionRight = rotateRight.mult(direction);
        Vector3f directionLeft = rotateLeft.mult(direction);
        
        leftRay = new Ray(vehicle.getPlayer().getPhysicsLocation().add(0, .25f, 0), directionLeft);
        rightRay = new Ray(vehicle.getPlayer().getPhysicsLocation().add(0, .25f, 0), directionRight);
 
        ((Node)rootNode.getChild("World")).collideWith(leftRay, leftResults);
        ((Node)rootNode.getChild("World")).collideWith(rightRay, rightResults);
        
        if (currentRoadLane == 0) {
            
        } else if (currentRoadLane < currentRoadNumberOfLanes) {
            if (leftResults.size() > 0 && leftResults.getClosestCollision().getDistance() < 10){
                Iterator<CollisionResult> itr = leftResults.iterator();
                
                while(itr.hasNext()) {
                    CollisionResult currentResult = itr.next();
                    
                    if (currentResult.getGeometry().getParent().getName().equals("RL_" + currentRoadNode.getName().substring(2) + "_" + (currentRoadLane - 1))) {
                        leftNormal = currentResult.getContactPoint();
                        leftQ = currentResult.getGeometry().getParent().getLocalRotation();
                        leftDirection = leftQ.toAngles(null);
                        leftDirection[1] += FastMath.PI / 2f;
                        leftQ = new Quaternion(leftDirection);
                    }
                    
                    if (currentResult.getGeometry().getParent().getName().equals("RL_" + currentRoadNode.getName().substring(2) + "_" + (currentRoadLane))) {
                        rightQ = currentResult.getGeometry().getParent().getLocalRotation();
                        rightDirection = rightQ.toAngles(null);
                        rightQ = new Quaternion(rightDirection);
                    }
                }
            }

            if (rightResults.size() > 0 && rightResults.getClosestCollision().getDistance() < 10){
                Iterator<CollisionResult> itr = rightResults.iterator();
                
                while(itr.hasNext()) {
                    CollisionResult currentResult = itr.next();
                    
                    if (currentResult.getGeometry().getParent().getName().equals("RL_" + currentRoadNode.getName().substring(2) + "_" + (currentRoadLane - 1))) {
                        leftQ = currentResult.getGeometry().getParent().getLocalRotation();
                        leftDirection = leftQ.toAngles(null);
                        leftDirection[1] += FastMath.PI / 2f;
                        leftQ = new Quaternion(leftDirection);
                    }
                    
                    if (currentResult.getGeometry().getParent().getName().equals("RL_" + currentRoadNode.getName().substring(2) + "_" + (currentRoadLane))) {
                        rightQ = currentResult.getGeometry().getParent().getLocalRotation();
                        rightDirection = rightQ.toAngles(null);
                        rightQ = new Quaternion(rightDirection);
                    }
                }
            }
            
            middleOfCurrentLane = leftNormal.add(rightNormal).divide(2f);
            if (leftQ != null) {
                vehicle.getPlayer().setPhysicsRotation(leftQ);
            }
        }
    }
    
    private void fixToMiddleOfLane() {
        Vector3f leftNormal = Vector3f.ZERO, rightNormal = Vector3f.ZERO, middleOfCurrentLane = vehicle.getPlayer().getPhysicsLocation();
        Quaternion rotateLeft = new Quaternion(), rotateRight = new Quaternion();
        rotateLeft.fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
        rotateRight.fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y);
        
        
        Ray leftRay, rightRay;
        
        CollisionResults leftResults = new CollisionResults();
        CollisionResults rightResults = new CollisionResults();
        Vector3f direction = new Vector3f();
        vehicle.getPlayer().getPhysicsRotation().getRotationColumn(0, direction);
        vehicle.getPlayer().getPhysicsRotation().getRotationColumn(1, direction);
        vehicle.getPlayer().getPhysicsRotation().getRotationColumn(2, direction);
        Vector3f directionRight = rotateRight.mult(direction);
        Vector3f directionLeft = rotateLeft.mult(direction);
        
        leftRay = new Ray(vehicle.getPlayer().getPhysicsLocation().add(0, .25f, 0), directionLeft);
        rightRay = new Ray(vehicle.getPlayer().getPhysicsLocation().add(0, .25f, 0), directionRight);
 
        ((Node)rootNode.getChild("World")).collideWith(leftRay, leftResults);
        ((Node)rootNode.getChild("World")).collideWith(rightRay, rightResults);
        
        if (currentRoadLane == 0) {
            
        } else if (currentRoadLane < currentRoadNumberOfLanes) {
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
            
            middleOfCurrentLane.setX((leftNormal.getX() + rightNormal.getX()) / 2f);
            middleOfCurrentLane.setY((leftNormal.getY() + rightNormal.getY()) / 2f);
            middleOfCurrentLane.setZ((leftNormal.getZ() + rightNormal.getZ()) / 2f);
            System.out.println("LeftNormal: " + CityHelper.toString(leftNormal));
            System.out.println("RightNormal: " + CityHelper.toString(rightNormal));
            System.out.println("MiddleOfCurrentLane: " + CityHelper.toString(middleOfCurrentLane));
            vehicle.getPlayer().setPhysicsLocation(middleOfCurrentLane);
            vehicle.getPlayer().setLinearVelocity(Vector3f.ZERO);
        }
    }
    
    public void reorient() {
        System.out.println("Reorienting...");
        steeringSensitivity = .05f;
        vehicle.setSteeringValue(0);
        vehicle.getPlayer().setAngularVelocity(Vector3f.ZERO);
        vehicle.setAccelerationValue(0);
        currentRoadNode = getCurrentRoadNode();
        fixDirection();
        System.out.println("Current road node: " + currentRoadNode.getName());
        if (currentRoadNode != null && currentRoadNode.getChildren().size() > 0) {
            int c = currentRoadNode.getChildren().size() - 1;
            
            while (!currentRoadNode.getChild(c).getName().startsWith("RL_")) {
                c--;
            }
            currentRoadNumberOfLanes = 2 + Integer.parseInt(currentRoadNode.getChild(c).getName().substring(currentRoadNode.getChild(c).getName().length()-1));
            currentRoadLane = determineCurrentRoadLane();           
            currentSpeedLimit = getCurrentRoadSpeedLimit();
            fixToMiddleOfLane();
            System.out.println("Current road: " + currentRoadNode.getName());
            System.out.println("Number of lanes: " + currentRoadNumberOfLanes);
            System.out.println("Current lane: " + currentRoadLane);
            System.out.println("Speed limit: " + currentSpeedLimit);
            System.out.println("Front location: " + getFrontVehicleLoc());
        }
        
        needsReorienting = false;
    }
    
    private void fixDirection() {
        float[] currentNodeDir = currentRoadNode.getLocalRotation().toAngles(null);
        //System.out.println("Current road node direction: " + CityHelper.toString(currentRoadNode.getWorldRotation().toAngles(null)));
        System.out.println("Car rotation before: " + CityHelper.toString(vehicle.getPlayer().getPhysicsRotation().toAngles(null)));
        currentNodeDir[1] += FastMath.HALF_PI;
        Quaternion q = new Quaternion();
        q = q.fromAngles(currentNodeDir);
        vehicle.getPlayer().setPhysicsRotation(q);
        System.out.println("Car rotation after: " + CityHelper.toString(vehicle.getPlayer().getPhysicsRotation().toAngles(null)));
    }
    private int determineCurrentRoadLane() {
        int currentLane;
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
        System.out.println("Right: (" + directionRight.x + " " + directionRight.y + " " + directionRight.z + ")");
        Vector3f directionLeft = rotateLeft.mult(direction);
        //directionLeft.normalizeLocal();
        System.out.println("Left: (" + directionLeft.x + " " + directionLeft.y + " " + directionLeft.z + ")");
        
        //System.out.println("LEFT: " + directionLeft.normalizeLocal());
        //System.out.println("RIGHT: " + directionRight.normalizeLocal());
        
        leftRay = new Ray(vehicle.getPlayer().getPhysicsLocation().add(0, .25f, 0), directionLeft);
        rightRay = new Ray(vehicle.getPlayer().getPhysicsLocation().add(0, .25f, 0), directionRight);
 
        ((Node)rootNode.getChild("World")).collideWith(leftRay, leftResults);
        ((Node)rootNode.getChild("World")).collideWith(rightRay, rightResults);

        if (leftResults.size() > 0 && leftResults.getClosestCollision().getDistance() < 100){
            leftLaneName = leftResults.getClosestCollision().getGeometry().getParent().getName();
            //System.out.println("Listing left collisions...");
            for(int i = 0; i < leftResults.size(); i++) {
                //System.out.println(leftResults.getCollision(i).getGeometry().getParent().getName());
            }
        }
        
        if (rightResults.size() > 0 && rightResults.getClosestCollision().getDistance() < 100){
            rightLaneName = rightResults.getClosestCollision().getGeometry().getParent().getName();
            //System.out.println("Listing right collisions...");
            for(int i = 0; i < rightResults.size(); i++) {
                //System.out.println(rightResults.getCollision(i).getGeometry().getParent().getName());
            }
        }
        
        //System.out.println("L" + leftLaneName + " R" + rightLaneName);
        
        if (!leftLaneName.startsWith("RL_")) { //Leftmost lane
            currentLane = 0;
            System.out.println(leftLaneName);
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