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
public class SimpleTrafficSignalPhase extends TrafficSignalPhase {
    /** Lights that should be green during this phase **/
    List<Node> signals;
    long greenTime, yellowTime, redTime;
    
    public SimpleTrafficSignalPhase() { 
        signals = new ArrayList<>();
    }
    
    public void addSignal(Node signal) {
        signals.add(signal);
    }

    public long getGreenTime() {
        return greenTime;
    }

    public void setGreenTime(long greenTime) {
        this.greenTime = greenTime;
    }

    public long getYellowTime() {
        return yellowTime;
    }

    public void setYellowTime(long yellowTime) {
        this.yellowTime = yellowTime;
    }

    public long getRedTime() {
        return redTime;
    }

    public void setRedTime(long redTime) {
        this.redTime = redTime;
    }
}