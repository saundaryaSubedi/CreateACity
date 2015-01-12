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
    private long timer;
    private boolean inPhase;
    
    public DefinedSignalControl(String name) {
        this.name = name;
        //this.signalList = signalList;
        phases = new ArrayList<>();
        inPhase = false;
    }
    
    @Override
    public void begin(Node root) {
        /*if (phases.isEmpty()) {
        } else {
            for (int i = 1; i < phases.size(); i++) {
                phases.get(i).setRed(root);
            }
        }*/
        currentPhase = 0;
        phases.get(currentPhase).begin(root);
        inPhase = true;
    }
    
    @Override
    public void update(float tpf, Node root) {
        if (phases.get(currentPhase) instanceof HazardTrafficSignalPhase) {
            phases.get(currentPhase).update(tpf, root);
        } else {
            if (inPhase) {
                if (phases.get(currentPhase).getState() == TrafficSignalPhaseState.RED) {
                    inPhase = false;
                    timer = System.currentTimeMillis();
                } else {
                    phases.get(currentPhase).update(tpf, root);
                }
            }
            if (!inPhase && (System.currentTimeMillis() - timer >= 1000)) {
                currentPhase++;
                if (currentPhase >= phases.size()) {
                    currentPhase = 0;
                }
                phases.get(currentPhase).begin(root);
                inPhase = true;
            }
        }
    }
    
    public void addPhase(TrafficSignalPhase p) {
        phases.add(p);
    }
    
        
}