package createacity;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import java.util.Arrays;

/**
 * A sensor can detect events and respond to them as necessary. Specifically,
 * the sensor responds to an object entering its definable region and then
 * performs an action.
 * @author Joseph
 */
public abstract class Sensor {
    
    /** The name of the sensor **/
    private String name;
    
    /** Boundaries of sensor, second **/
    protected Vector3f[][] boundaries;
    
    /** Whether the sensor is activated **/
    protected boolean hot;
    
    /** How long the sensor has been activated **/
    protected float timeHot;
    
    /** When the sensor was first activated **/
    protected long timeSinceFirstActivated;
    
    public Sensor(String name, Vector3f[][] locations) {
        if (locations == null) {
            throw new IllegalArgumentException("Sensor locations cannot be null");
        }
        
        this.name = name;
        
        boundaries = new Vector3f[locations.length][2];
        boundaries = Arrays.copyOf(locations, locations.length);
        hot = false;
        timeHot = 0;
        timeSinceFirstActivated = 0;
    }
    
    public abstract void check(Node collisionNode, float tpf);
        
    public boolean isHot() {
        return hot;
    }
    
    public String getName() {
        return name;
    }
    
    public float getTimeHot() {
        return timeHot;
    }

    @Override
    public String toString() {
        return "Sensor{" + "name=" + name + ", boundaries=" + getBoundaries() + ", hot=" + hot + ", timeHot=" + timeHot + '}';
    }
    
    public String getBoundaries() {
        String s = "[";
        
        for(int i = 0; i < boundaries.length; i++) {
            s += "[" + boundaries[i][0] + ", " + boundaries[i][1] + "]";
        }
        
        s += "]";
        
        return s;
    }
}
