package createacity;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Spline;
import com.jme3.math.Spline.SplineType;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Curve;

/*** A list of waypoints***/
public class WayList {
    private Vector3f[] waypoints;
    private boolean debug;
    
    public WayList(Vector3f[] points){
        waypoints = points;
        debug = false;
    }
    
    public void update(AssetManager assetManager, Node rootNode){
        if (debug) {
            drawDebug(assetManager, rootNode);
        }
    }
    
    private void drawDebug(AssetManager assetManager, Node rootNode){
        /*for(int i=0; i<waypoints.length-1; i++){
            float angle, xExtent, yExtent, zExtent;
            
            System.out.println(angleBetween(waypoints[i], waypoints[i+1]) * (180f/FastMath.PI));
            
            Vector3f middle = new Vector3f((waypoints[i].x + waypoints[i+1].x) / 2, (waypoints[i].y + waypoints[i+1].y) / 2, (waypoints[i].z + waypoints[i+1].z) / 2);
            angle = waypoints[i].angleBetween(waypoints[i+1]);
            
            //System.out.println(middle);
            Node pivotNode = new Node("WayPoint Pivot Node");
            if (FastMath.abs(middle.x) < 5f)
                xExtent = 5f;
            else
                xExtent = middle.x;
            
            if (FastMath.abs(middle.y) < 5)
                yExtent = 5f;
            else
                yExtent = middle.y;
            
            if (FastMath.abs(middle.z) < 5)
                zExtent = 5f;
            else
                zExtent = middle.z;
            
            Box drawBox = new Box(middle, 5f, yExtent, zExtent);
            Geometry drawGeom = new Geometry("WayPoint Node", drawBox);
            //System.out.println(drawGeom.getLocalScale());
            pivotNode.setLocalTranslation(waypoints[i]);
            pivotNode.rotate(0, angle, 0);
            Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");;
            boxMat.setColor("Color", ColorRGBA.White);
            drawGeom.setMaterial(boxMat);
            //pivotNode.attachChild(drawGeom);
            rootNode.attachChild(drawGeom);
        }*/
        
        Spline spline = new Spline(SplineType.Linear, waypoints, 1, false);
        Curve curve = new Curve(spline, 1);
        Geometry drawGeom = new Geometry("WayPoint Node", curve);
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");;
        boxMat.setColor("Color", ColorRGBA.White);
        drawGeom.setMaterial(boxMat);
        rootNode.attachChild(drawGeom);
    }
    
    private float angleBetween(Vector3f origin, Vector3f point){
        float angle;
        
        System.out.println("Distance: " + (point.subtract(origin)));
        System.out.println("Adjacent: " + (origin.x-point.x));
        
        //Should be point.x-origin.x
        angle = FastMath.acos((origin.x-point.x) / (point.subtract(origin)).length());
        
        return angle;
    }
    
    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public Vector3f[] getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(Vector3f[] waypoints) {
        this.waypoints = waypoints;
    }
}
