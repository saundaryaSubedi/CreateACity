package createacity;

import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import javax.swing.JOptionPane;
import java.util.Scanner;
import java.util.concurrent.Callable;


public class CityHelper {
    public static int numberOfBoxes = 0;
    
    public static void moveCamera(Camera camera){
        String loc = JOptionPane.showInputDialog(null, "Enter XYZ location separated by spaces ONLY (format: X Y Z):", "Enter Location", JOptionPane.QUESTION_MESSAGE);
        if (loc == null)
            return;
        Scanner input = new Scanner(loc);
        camera.setLocation(new Vector3f(input.nextFloat(), input.nextFloat(), input.nextFloat()));
    }
    
    public static float kphtoms(float kph) {
        return kph * (5f/18f);
    }
    
    public static float mstokph(float ms) {
        return ms * (18/5f);
    }
    
    public static void placeBox(Node parentNode, AssetManager assetManager) {
        String locString = JOptionPane.showInputDialog(null, "Enter box XYZ location separated by spaces ONLY (format: X Y Z):", "Enter Box Location", JOptionPane.QUESTION_MESSAGE);
        if (locString == null)
            return;
        Scanner input = new Scanner(locString);
        Vector3f loc = new Vector3f(input.nextFloat(), input.nextFloat(), input.nextFloat());
        Box box = new Box(loc, .5f, .5f, .5f);
        Geometry boxGeom = new Geometry(Integer.toString(numberOfBoxes), box);
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boxMat.setColor("Color", ColorRGBA.Blue);
        boxGeom.setMaterial(boxMat);
        parentNode.attachChild(boxGeom);
    }
    
    public static void findNearestStreet(Node world) {
        String locString = JOptionPane.showInputDialog(null, "Enter XYZ location separated by spaces ONLY (format: X Y Z):", "Enter Location", JOptionPane.QUESTION_MESSAGE);
        if (locString == null)
            return;
        Scanner input = new Scanner(locString);
        Vector3f loc = new Vector3f(input.nextFloat(), input.nextFloat(), input.nextFloat());
        
    }
    
    public static Vector3f getLocation() {
        String locString = JOptionPane.showInputDialog(null, "Enter XYZ location separated by spaces ONLY (format: X Y Z):", "Enter Location", JOptionPane.QUESTION_MESSAGE);
        if (locString == null)
            return Vector3f.ZERO;
        Scanner input = new Scanner(locString);
        Vector3f loc = new Vector3f(input.nextFloat(), input.nextFloat(), input.nextFloat());
        return loc;
    }    
    
    public static String toString(Vector3f v) {
        if (v == null) {
            return null;
        }
        
        String s = "(";
        s += v.getX() + ", ";
        s += v.getY() + ", ";
        s += v.getZ() + ")" ;
        
        return s;
    }
    
    public static String toString(float[] a) {
        if (a.length != 3) {
            return null;
        }
        
        String s = "(";
        s += a[0] + ", ";
        s += a[1] + ", ";
        s += a[2] + ")" ;
        
        return s;
    }
}
