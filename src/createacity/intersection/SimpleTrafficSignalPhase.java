/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package createacity.intersection;

import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Joseph
 */
public class SimpleTrafficSignalPhase extends TrafficSignalPhase {
    /** Lights that should be active and inactive during this phase **/
    protected Map<String, List<Node>> signals;
    protected long greenTime, yellowTime, redTime;
    protected long timer;
    protected TrafficSignalPhaseState state;
    protected List<PointLight> redLights, yellowLights, greenLights;
    
    public SimpleTrafficSignalPhase(List<Node> activeSignals, List<Node> inactiveSignals) { 
        signals = new HashMap<>();
        redLights = new ArrayList<>();
        yellowLights = new ArrayList<>();
        greenLights = new ArrayList<>();
        signals.put(TrafficSignalPhase.ACTIVE_SIGNALS, activeSignals);
        signals.put(TrafficSignalPhase.INACTIVE_SIGNALS, inactiveSignals);
        state = TrafficSignalPhaseState.UNDEFINED;
    }
    
    public SimpleTrafficSignalPhase() {
        signals = new HashMap<>();
        redLights = new ArrayList<>();
        yellowLights = new ArrayList<>();
        greenLights = new ArrayList<>();
        state = TrafficSignalPhaseState.UNDEFINED;
    }
    
    public void addActiveSignals(List<Node> activeSignals) {
        signals.put(TrafficSignalPhase.ACTIVE_SIGNALS, activeSignals);
    }
    
    public void addInactiveSignals(List<Node> inactiveSignals) {
        signals.put(TrafficSignalPhase.INACTIVE_SIGNALS, inactiveSignals);
    }
    
    @Override
    public void begin(Node root) {
        setGreen(root);   
        removeYellow(root);
        removeRed(root);
        setRed(root, signals.get(TrafficSignalPhase.INACTIVE_SIGNALS));
    }
    
    public void setGreen(Node root) {
        setGreen(root, signals.get(TrafficSignalPhase.ACTIVE_SIGNALS));
        state = TrafficSignalPhaseState.GREEN;
        timer = System.currentTimeMillis();
    }
    
    @Override
    public void setGreen(Node root, List<Node> signalList) {
        /*for (int i = 0; i < root.getChildren().size(); i++) {
            if (root.getChild(i).getName().startsWith("SIGNALLIGHT_" + )
        }*/
        
        if (signalList != null) {
            //Make all lights green
            for(Node signal: signalList) {
                for (int i = 0; i < signal.getChildren().size(); i++) {
                    if (signal.getChild(i) instanceof Node) {
                        Node lightNode = (Node) signal.getChild(i);
                        if (lightNode.getName().startsWith("SIGNALLIGHT_") && lightNode.getName().endsWith("2")) {

                            PointLight greenLight = new PointLight();
                            greenLight.setColor(ColorRGBA.Green.mult(SignalizedIntersectionUtility.getLightIntensity(lightNode)));
                            greenLight.setRadius(SignalizedIntersectionUtility.getLightRadius(lightNode));
                            greenLight.setPosition(SignalizedIntersectionUtility.getLightPosition(lightNode));

                            root.addLight(greenLight);
                            greenLights.add(greenLight);

                            //Attaching to root while also detaching from signal
                            ((Node)root.getChild(signal.getName())).attachChild(lightNode);
                        }
                    }
                }
            }
        }
    }
    
    public void setYellow(Node root) {
        setYellow(root, signals.get(TrafficSignalPhase.ACTIVE_SIGNALS));
        state = TrafficSignalPhaseState.YELLOW;
        timer = System.currentTimeMillis();
    }
    
    @Override
    public void setYellow(Node root, List<Node> signalList) {
        if (signalList != null) {
            //Make all lights yellow
            for(Node signal: signalList) {
                for (int i = 0; i < signal.getChildren().size(); i++) {
                    if (signal.getChild(i) instanceof Node) {
                        Node lightNode = (Node) signal.getChild(i);
                        if (lightNode.getName().startsWith("SIGNALLIGHT_") && lightNode.getName().endsWith("1")) {

                            PointLight yellowLight = new PointLight();
                            yellowLight.setColor(ColorRGBA.Yellow.mult(SignalizedIntersectionUtility.getLightIntensity(lightNode)));
                            yellowLight.setRadius(SignalizedIntersectionUtility.getLightRadius(lightNode));
                            yellowLight.setPosition(SignalizedIntersectionUtility.getLightPosition(lightNode));

                            root.addLight(yellowLight);
                            yellowLights.add(yellowLight);

                            //Attaching to root while also detaching from signal
                            ((Node)root.getChild(signal.getName())).attachChild(lightNode);
                        }
                    }
                }
            }
        }
    }
    
    public void setRed(Node root) {
        setRed(root, signals.get(TrafficSignalPhase.ACTIVE_SIGNALS));
        state = TrafficSignalPhaseState.RED;
        timer = System.currentTimeMillis();
    }
    
    @Override
    public void setRed(Node root, List<Node> signalList) {
        if (signalList != null) {
            //Make all lights red
            for(Node signal: signalList) {
                for (int i = 0; i < signal.getChildren().size(); i++) {
                    if (signal.getChild(i) instanceof Node) {
                        Node lightNode = (Node) signal.getChild(i);
                        if (lightNode.getName().startsWith("SIGNALLIGHT_") && lightNode.getName().endsWith("0")) {

                            PointLight redLight = new PointLight();
                            redLight.setColor(ColorRGBA.Red.mult(SignalizedIntersectionUtility.getLightIntensity(lightNode)));
                            redLight.setRadius(SignalizedIntersectionUtility.getLightRadius(lightNode));
                            redLight.setPosition(SignalizedIntersectionUtility.getLightPosition(lightNode));

                            root.addLight(redLight);
                            redLights.add(redLight);

                            //Attaching to root while also detaching from signal
                            ((Node)root.getChild(signal.getName())).attachChild(lightNode);
                        }
                    }
                }
            }  
        }
    }
    
    public void removeGreen(Node root) {
        //Remove all green lights
        for(Node signal: signals.get(TrafficSignalPhase.ACTIVE_SIGNALS)) {
            for (int i = 0; i < ((Node)root.getChild(signal.getName())).getChildren().size(); i++) {
                Spatial n = ((Node)root.getChild(signal.getName())).getChild(i);

                if (n.getName().startsWith("SIGNALLIGHT_") && n.getName().endsWith("2")) {
                   //Detaching from root while also attaching to signal
                    signal.attachChild(n);
                    for (int j = 0; j < greenLights.size(); j++) {
                        root.removeLight(greenLights.get(j));
                    }
                }
            }
        }
    }
    
    public void removeYellow(Node root) {
        //Remove all yellow lights
        for(Node signal: signals.get(TrafficSignalPhase.ACTIVE_SIGNALS)) {
            for (int i = 0; i < ((Node)root.getChild(signal.getName())).getChildren().size(); i++) {
                Spatial n = ((Node)root.getChild(signal.getName())).getChild(i);
                
                if (n.getName().startsWith("SIGNALLIGHT_") && n.getName().endsWith("1")) {
                    //Detaching from root while also attaching to signal
                    signal.attachChild(n);
                    for (int j = 0; j < yellowLights.size(); j++) {
                        root.removeLight(yellowLights.get(j));
                    }
                }
            }
        }
    }
    
    public void removeRed(Node root) {
        //Remove all red lights
        for(Node signal: signals.get(TrafficSignalPhase.ACTIVE_SIGNALS)) {
            for (int i = 0; i < ((Node)root.getChild(signal.getName())).getChildren().size(); i++) {
                Spatial n = ((Node)root.getChild(signal.getName())).getChild(i);
                
                if (n.getName().startsWith("SIGNALLIGHT_") && n.getName().endsWith("0")) {
                    //Detaching from root while also attaching to signal
                    signal.attachChild(n);
                    for (int j = 0; j < redLights.size(); j++) {
                        root.removeLight(redLights.get(j));
                    }
                }
            }
        }
    }
    
    @Override
    public void update(float tpf, Node root) {
        switch (state) {
            case GREEN:
                if (System.currentTimeMillis() - timer >= greenTime) {
                    removeGreen(root);
                    setYellow(root);
                }
                break;
            case YELLOW:
                if (System.currentTimeMillis() - timer >= yellowTime) {
                    removeYellow(root);
                    setRed(root);
                }
                break;
            case RED:
                if (System.currentTimeMillis() - timer >= redTime) {
                    removeRed(root);
                    setGreen(root);
                }
                 break;
            case UNDEFINED:
                 break;
        }
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
    
    @Override
    public TrafficSignalPhaseState getState() {
        return state;
    }
}