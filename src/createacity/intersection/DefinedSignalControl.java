/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package createacity.intersection;

import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Joseph
 */
public class DefinedSignalControl extends SignalControl {
    int currentPhase;
    //List<Node> signalList;
    List<TrafficSignalPhase> phases;
    
    public DefinedSignalControl(String name) {
        this.name = name;
        //this.signalList = signalList;
        phases = new ArrayList<>();
    }
    
    public void addPhase(TrafficSignalPhase p) {
        phases.add(p);
    }
    
        
}
