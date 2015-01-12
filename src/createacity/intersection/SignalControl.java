/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package createacity.intersection;

import com.jme3.scene.Node;

/**
 *
 * @author Joseph
 */
public abstract class SignalControl {
    protected String name;
    
    public abstract void begin(Node root);
    public abstract void update(float tpf, Node root);
    
    public String getName() {
        return name;
    }
}