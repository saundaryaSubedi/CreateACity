//Copyright 2012 New York City 3D Community

//This file is part of New York City 3D.

//New York City 3D is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//New York City 3D is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with New York City 3D.  If not, see <http://www.gnu.org/licenses/>.
package createacity.objects.streetlight;

import com.jme3.light.Light;
import com.jme3.scene.Node;
import java.util.ArrayList;

/**
 * Streetlight
 */
public class Streetlight {
   private ArrayList<Light> removedLights = new ArrayList<>(); 
   private String id; 
   
   public Streetlight(Node model){
       id = model.getName();
   }
   
   public void turnOff(Node root){
       for (int i = 0; i < root.getLocalLightList().size(); i++){
           if (root.getLocalLightList().get(i).getName().contains("StreetLightLamp" + id)){
               removedLights.add(root.getLocalLightList().get(i));
               root.removeLight(root.getLocalLightList().get(i));
               i--;
           }
       }
   }
   
   public void turnOn(Node root){
       for(Light l: removedLights) {
           root.addLight(l);
       }
   }
}
