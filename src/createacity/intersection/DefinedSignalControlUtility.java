/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package createacity.intersection;

import com.jme3.scene.Node;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Joseph
 */
public class DefinedSignalControlUtility {
    
    public static ArrayList<DefinedSignalControl> addControls(String fileName, Node signals) {
        Scanner scan;
        ArrayList<DefinedSignalControl> allSignalControls = new ArrayList<>();
        
        try {
            scan = new Scanner(new File(fileName));
            while (scan.hasNext()) {
                List<DefinedSignalControl> c = addControls(scan, signals);
                if (c != null) {
                    allSignalControls.addAll(c);
                } else {
                    return allSignalControls;
                }
            }
        } catch (FileNotFoundException e) {
            return null;
        }
        
        return allSignalControls;
    }
    
    private static List<DefinedSignalControl> addControls(Scanner s, Node signals) {
        List<DefinedSignalControl> controls = new ArrayList<>();
        String name = s.next();
        s.nextLine();
        int type = s.nextInt();
        int numPhases = s.nextInt();
        int a;
        List<List<Node>> signalsList = new ArrayList<>();
        
        for(a = 0; a < signals.getChildren().size() && !signals.getChild(a).getName().equals(name); a++) {}

        Node parent = (Node)signals.getChild(a);    //Main intersection node
        switch(type) {
            case 0:                         //Hazard
                for (int i = 0; i < numPhases; i++) {
                    DefinedSignalControl control = new DefinedSignalControl(name + "_" + i);
                    List<Node> phaseSignals = new ArrayList<>();
                    while(!s.hasNextLong()) {
                        String signalName = s.next();
                        int j;
                        for(j = 0; j < parent.getChildren().size(); j++) {
                            if (parent.getChild(j).getName().equals(signalName)) {
                                phaseSignals.add((Node)parent.getChild(j));
                            }
                        } 
                    }
                    
                    long activeTime = s.nextLong();
                    long blankTime = s.nextLong();
                    
                    HazardTrafficSignalPhase hazardPhase;
                    if (i == 0) {       //Flashing yellow
                        hazardPhase = new HazardTrafficSignalPhase(blankTime, TrafficSignalPhaseState.YELLOW, phaseSignals);
                        hazardPhase.setYellowTime(activeTime);
                    } else {            //Flashing red
                        hazardPhase = new HazardTrafficSignalPhase(blankTime, TrafficSignalPhaseState.RED, phaseSignals);
                        hazardPhase.setRedTime(activeTime);
                    }
                    
                    control.addPhase(hazardPhase);
                    controls.add(control);
                }
                break;
            case 1:                         //Simple
                DefinedSignalControl control = new DefinedSignalControl(name);
                List<SimpleTrafficSignalPhase> phaseList = new ArrayList<>();
                for (int i = 0; i < numPhases; i++) {
                    List<Node> phaseSignals = new ArrayList<>();
                    while(!s.hasNextLong()) {
                        String signalName = s.next();
                        int j;
                        for(j = 0; j < parent.getChildren().size(); j++) {
                            if (parent.getChild(j).getName().equals(signalName)) {
                                phaseSignals.add((Node)parent.getChild(j));
                            }
                        }

                        /*if (j == parent.getChildren().size()) {
                            p.addSignal(new Node(signalName));
                        }*/
                    }
                    signalsList.add(phaseSignals);
                    SimpleTrafficSignalPhase p = new SimpleTrafficSignalPhase();
                    p.setGreenTime(s.nextLong());
                    p.setYellowTime(s.nextLong());
                    p.setRedTime(s.nextLong());
                    phaseList.add(p);
                }
                
                for (int i = 0; i < phaseList.size(); i++) {
                    phaseList.get(i).addActiveSignals(signalsList.get(i));
                    
                    for (int j = 0; j < i; j++) {
                        phaseList.get(i).addInactiveSignals(signalsList.get(j));
                    }
                    
                    for (int j = i+1; j < signalsList.size(); j++) {
                        phaseList.get(i).addInactiveSignals(signalsList.get(j));
                    }
                    control.addPhase(phaseList.get(i));
                }
                controls.add(control);
                
                break;
        }
        /*for (int i = 0; i < numPhases; i++) {
            SimpleTrafficSignalPhase p = new SimpleTrafficSignalPhase();
            while(!s.hasNextLong()) {
                String signalName = s.next();
                int j;
                for(j = 0; j < parent.getChildren().size(); j++) {
                    if (parent.getChild(j).getName().equals(signalName)) {
                        p.addSignal((Node)parent.getChild(j));
                    }
                }
                
                if (j == parent.getChildren().size()) {
                    p.addSignal(new Node(signalName));
                }
            }
            p.setGreenTime(s.nextLong());
            p.setYellowTime(s.nextLong());

            if (i == numPhases - 1) {
                p.setRedTime(s.nextLong());
            }

            control.addPhase(p);
        }*/
        
        return controls;
    }
}
