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
public class HazardTrafficSignalPhase extends SimpleTrafficSignalPhase{
    public static final long DEFAULT_BLANK_TIME = 1000;
    
    private long blankTime;
    private TrafficSignalPhaseState hazardState;
    
    public HazardTrafficSignalPhase(long blankTime, TrafficSignalPhaseState hazardState, List<Node> activeSignals) {
        super(activeSignals, null);
        this.blankTime = blankTime;
        this.hazardState = hazardState;
    }

    @Override
    public void begin(Node root) {
        switch (hazardState) {
            case GREEN:
                setGreen(root);
                break;
            case YELLOW:
                setYellow(root);
                break;
            case RED:
                setRed(root);
                break;
            default:
                setYellow(root);
                break;
        }
        
    }

    @Override
    public void update(float tpf, Node root) {
        switch (state) {
            case GREEN:
                if (System.currentTimeMillis() - timer >= greenTime) {
                    removeGreen(root);
                    state = TrafficSignalPhaseState.BLANK;
                    timer = System.currentTimeMillis();
                }
                break;
            case YELLOW:
                if (System.currentTimeMillis() - timer >= yellowTime) {
                    removeYellow(root);
                    state = TrafficSignalPhaseState.BLANK;
                    timer = System.currentTimeMillis();
                }
                break;
            case RED:
                if (System.currentTimeMillis() - timer >= redTime) {
                    removeRed(root);
                    state = TrafficSignalPhaseState.BLANK;
                    timer = System.currentTimeMillis();
                }
                break;
            case UNDEFINED:
                if (System.currentTimeMillis() - timer >= yellowTime) {
                    removeYellow(root);
                    state = TrafficSignalPhaseState.BLANK;
                    timer = System.currentTimeMillis();
                }
                break;
            case BLANK:
                if (System.currentTimeMillis() - timer >= blankTime) {
                    begin(root);
                }
                break;
        }
    }
    
}

