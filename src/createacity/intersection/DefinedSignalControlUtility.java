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
    
    public static ArrayList<DefinedSignalControl> addControls(String fileName) {
        Scanner scan;
        ArrayList<DefinedSignalControl> allSignalControls = new ArrayList<>();
        
        try {
            scan = new Scanner(new File(fileName));
            while (scan.hasNext()) {
                DefinedSignalControl c = addControl(scan);
                if (c != null) {
                    allSignalControls.add(c);
                } else {
                    return allSignalControls;
                }
            }
        } catch (FileNotFoundException e) {
            return null;
        }
        
        return allSignalControls;
    }
    
    private static DefinedSignalControl addControl(Scanner s) {
        DefinedSignalControl control;
        String name = s.nextLine();
        int type = s.nextInt();
        int numPhases = s.nextInt();

        control = new DefinedSignalControl(name);

        for (int i = 0; i < numPhases; i++) {
            SimpleTrafficSignalPhase p = new SimpleTrafficSignalPhase();
            while(!s.hasNextLong()) {
                p.addSignal(new Node(s.next()));
            }
            p.setGreenTime(s.nextLong());
            p.setYellowTime(s.nextLong());

            if (i == numPhases - 1) {
                p.setRedTime(s.nextLong());
            }

            control.addPhase(p);
        }
        
        return control;
    }
}
