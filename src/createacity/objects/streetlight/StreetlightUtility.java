/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package createacity.objects.streetlight;

import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Joseph
 */
public class StreetlightUtility {
    public static final float DEFAULT_STREETLIGHT_RANGE = 15f;
    public static final float DEFAULT_STREETLIGHT_INNER_ANGLE = 45f;
    public static final float DEFAULT_STREETLIGHT_OUTER_ANGLE = 75f;
    
    private static List<SpotLight> allStreetlights = new ArrayList<>();
    
    public static void initStreetlights(Node streetlights) {
        for (int i = 0; i < streetlights.getChildren().size(); i++) {
            if (streetlights.getChild(i) instanceof Node) {
                Node streetlight = (Node) streetlights.getChild(i);
                
                for (int j = 0; j < streetlight.getChildren().size(); j++) {
                    if (streetlight.getChild(j) instanceof Node) {
                        Node streetlightBulb = (Node) streetlight.getChild(j);
                        
                        if (streetlightBulb.getName().startsWith("SLB_")) {
                            SpotLight spot = new SpotLight();
                            spot.setSpotRange(DEFAULT_STREETLIGHT_RANGE);                           // distance
                            spot.setSpotInnerAngle(DEFAULT_STREETLIGHT_INNER_ANGLE * FastMath.DEG_TO_RAD); // inner light cone (central beam)
                            spot.setSpotOuterAngle(DEFAULT_STREETLIGHT_OUTER_ANGLE * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
                            spot.setColor(ColorRGBA.White);         // light color
                            spot.setDirection(new Vector3f(0f, -1f, 0f));
                            spot.setPosition(streetlightBulb.getWorldTranslation());
                            allStreetlights.add(spot);
                            
                        }
                    }
                }
            }
        }
    }
    
    public static void turnOnAllStreetlights(Node displayNode) {
        for(SpotLight spot: allStreetlights) {
            displayNode.addLight(spot);
        }
    }
    
    public static void turnOffAllStreetlights(Node displayNode) {
        for(SpotLight spot: allStreetlights) {
            displayNode.removeLight(spot);
        }
    }
}
