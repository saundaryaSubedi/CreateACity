/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package createacity.intersection;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.ArrayList;

/**
 *
 * @author Joseph
 */
public class SignalizedIntersectionUtility {
    public static final float DEFAULT_LIGHT_INTENSITY = .25f;
    public static final float DEFAULT_LIGHT_RADIUS = 15f;
    
    public static ArrayList<SignalizedIntersection> buildIntersections(ArrayList<DefinedSignalControl> controlList, Node signalNode) {
        ArrayList<SignalizedIntersection> intersections = new ArrayList<>();
        
        for (SignalControl c: controlList) {
            for (int i = 0; i < signalNode.getChildren().size(); i++) {
                System.out.println("c: " + c.getName());
                System.out.println("signalNode.getChild(i): " + signalNode.getChild(i).getName());
                if (c.getName().equals(signalNode.getChild(i).getName())) {
                    intersections.add(new SignalizedIntersection((Node) signalNode.getChild(i).clone(), c));
                }
            }
        }
        
        return intersections;
    }
    
    /**
     * Returns the position in front of the light mesh so that the light
     * properly illuminates the mesh
     * @param lightNode the node of the light mesh
     * @return the position to place the light
     */
    public static Vector3f getLightPosition(Node lightNode) {
        Vector3f pos = lightNode.getWorldTranslation();
        
        if (lightNode.getUserData("dir") != null) {
            String dir = lightNode.getUserData("dir");

            switch (dir) {
                case "X+":
                    return pos.add(Vector3f.UNIT_X);
                case "X-":
                    return pos.subtract(Vector3f.UNIT_X);
                case "Z+":
                    return pos.add(Vector3f.UNIT_Z);
                case "Z-":
                    return pos.subtract(Vector3f.UNIT_Z);
                default:
                    return pos;
            }
        } else {
            return pos;
        }
    }
    
    /**
     * Returns the intensity of the light mesh provided in the custom properties
     * @param lightNode the node of the light mesh
     * @return the intensity that the light should be
     */
    public static float getLightIntensity(Node lightNode) {
        float intensity = DEFAULT_LIGHT_INTENSITY;
        
        if (lightNode.getUserData("lightIntensity") != null) {
            intensity = Float.parseFloat(lightNode.getUserData("lightIntensity").toString());
        }
        
        return intensity;
    }
    
    /**
     * Returns the radius of the light mesh provided in the custom properties
     * @param lightNode the node of the light mesh
     * @return the radius that the light should be
     */
    public static float getLightRadius(Node lightNode) {
        float radius = DEFAULT_LIGHT_RADIUS;
        
        if (lightNode.getUserData("lightRadius") != null) {
            radius = Float.parseFloat(lightNode.getUserData("lightRadius").toString());
        }
        
        return radius;
    }
}
