/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package createacity.intersection;

import com.jme3.scene.Node;
import createacity.NodeInspector;
import java.util.ArrayList;

/**
 *
 * @author Joseph
 */
public class SignalizedIntersectionUtility {
    public static ArrayList<SignalizedIntersection> buildIntersections(ArrayList<DefinedSignalControl> controlList, Node signalNode) {
        ArrayList<SignalizedIntersection> intersections = new ArrayList<>();
        
        NodeInspector.inspectNode(0, signalNode);
        
        for (SignalControl c: controlList) {
            for (int i = 0; i < signalNode.getChildren().size(); i++) {
                System.out.println("c: " + c.getName());
                System.out.println("signalNode.getChild(i): " + signalNode.getChild(i).getName());
                if (c.getName().equals(signalNode.getChild(i).getName())) {
                    intersections.add(new SignalizedIntersection((Node) signalNode.getChild(i).clone(), c));
                }
            }
        }
        
        return intersections;
    }
}
