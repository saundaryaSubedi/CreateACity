/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package createacity.intersection;

import com.jme3.scene.Node;
import java.util.List;

/**
 *
 * @author Joseph
 */
public class SignalizedIntersection extends Intersection {
    SignalControl control;
    Node parent;
 
    public SignalizedIntersection(Node parent, SignalControl control) {
        this.parent = parent;
        this.control = control;
    }
}
