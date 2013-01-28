/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package createacity;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author Joseph
 */
public class VehicleSensor extends Sensor{

    public VehicleSensor(String name, Vector3f[][] locations) {
        super(name, locations);
    }
    
    /**
     * Detects if a vehicle is on the sensor. A vehicle is on the sensor if 
     * any of the location points intersect with any part of a vehicle as its
     * closest collision result.
     */
    @Override
    public void check(Node collisionNode, float tpf) {
        CollisionResults results;
        
        for(int i = 0; i < boundaries.length; i++) {
            results = new CollisionResults();
            Ray ray = new Ray(boundaries[i][0], boundaries[i][1]);
            collisionNode.collideWith(ray, results);
            
            if (results.size() > 0) {
                hot = true;
                timeHot += tpf;
                return;
            }
        }
        
        hot = false;
        timeHot = 0f;
    }
    
}
