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
public abstract class TrafficSignalPhase {
    public static final String ACTIVE_SIGNALS = "ACTIVE_SIGNALS";
    public static final String INACTIVE_SIGNALS = "INACTIVE_SIGNALS";
    
    public abstract void begin(Node root);
    public abstract void setGreen(Node root, List<Node> signalList);
    public abstract void setYellow(Node root, List<Node> signalList);
    public abstract void setRed(Node root, List<Node> signalList);
    public abstract void update(float tpf, Node root);
    public abstract TrafficSignalPhaseState getState();
}