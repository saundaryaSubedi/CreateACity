//Copyright 2011 New York City 3D Community

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

package createacity;

import com.jme3.collision.CollisionResults;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.screen.Screen;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

public class HUD{
    private String currentStreet, currentTime, nextStreet;
    private AppSettings settings;
    private final String DATE_FORMAT = "h:mm a";
    private Screen hudScreen;
    private boolean onRoad;

    public HUD(AppSettings settings){
        this.settings = settings;
        onRoad = true;
    }

    public void update(Node rootNode, Camera cam, Nifty nifty, HashMap<String, StreetInfo> streetInfoMap){      
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        currentTime = sdf.format(cal.getTime());
        
        Ray ray;
        
        CollisionResults results = new CollisionResults();

        ray = new Ray(cam.getLocation(), new Vector3f(0, -1, 0));
 
        rootNode.getChild("World").collideWith(ray, results);

        if (results.size() > 0 && results.getClosestCollision().getDistance() < 500){
            String roadObj = results.getClosestCollision().getGeometry().getParent().getName();
            float roadDirection[] = results.getClosestCollision().getGeometry().getParent().getLocalRotation().toAngles(null);
            //System.out.println(roadDirection[0] + " " + roadDirection[1] + " " + roadDirection[2]);
            roadDirection[0] += FastMath.PI / 2f;

            /** BEGIN TEMPORARY DIRECTION CODE**/          
            Vector3f roadDir = new Quaternion().fromAngles(roadDirection).getRotationColumn(0),
                     oppositeRoadDir = roadDir.negate(),
                     camDir = cam.getDirection();
            /*System.out.println(roadDir);
            System.out.println(camDir);
            float temp = camDir.getX();
            camDir.setX(camDir.getZ());
            camDir.setZ(temp);*/
            
            
            if (camDir.distance(oppositeRoadDir) < camDir.distance(roadDir)) {
                onRoad = false;
            }
            else {
                onRoad = true;
            }         
            /** END TEMPORARY DIRECTION CODE**/
            
            if (streetInfoMap.get(roadObj) != null){
                currentStreet = streetInfoMap.get(roadObj).getStreetName();
                if (onRoad) {
                    nextStreet = "Next: " + streetInfoMap.get(roadObj).getNextStreetName();
                }
                else {
                    nextStreet = "Next: " + streetInfoMap.get(roadObj).getPrevStreetName();
                }               
            } else {
                currentStreet = nextStreet = "";
            }
            

            /*if (!nifty.getCurrentScreen().isNull() && nifty.getCurrentScreen().getScreenId().equals("hud")){           
                nifty.getCurrentScreen().findNiftyControl("currentStreet", Label.class).setText(currentStreet);
                
                nifty.getCurrentScreen().findNiftyControl("currentTime", Label.class).setText(currentTime);
            }*/
        }
        else{
            currentStreet = "";
            nextStreet = "";
        }
        
        if (nifty.getCurrentScreen() != null && nifty.getCurrentScreen().getScreenId().equals("hud")){           
            nifty.getCurrentScreen().findNiftyControl("currentStreet", Label.class).setText(currentStreet);
            nifty.getCurrentScreen().findNiftyControl("nextStreet", Label.class).setText(nextStreet);
            nifty.getCurrentScreen().findNiftyControl("currentSpeedKPH", Label.class).setText("");                
            nifty.getCurrentScreen().findNiftyControl("currentTime", Label.class).setText(currentTime);
        }   
    }
    
    public void updateDriving(Node rootNode, Node worldNode, Vehicle car, Nifty nifty, HashMap<String, StreetInfo> streetInfoMap, HashMap<String, IntersectionInfo> intersectionInfoMap){      
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        currentTime = sdf.format(cal.getTime());
        
        Vector3f pos = car.getCarNode().getWorldTranslation(); 
        Vector3f dir = car.getPlayer().getForwardVector(null);
        
        Ray ray;
        
        CollisionResults results = new CollisionResults();

        ray = new Ray(pos, new Vector3f(0, -1, 0));
 
        rootNode.getChild("World").collideWith(ray, results);

        if (results.size() > 0 && results.getClosestCollision().getDistance() < 500){
            String roadObj = results.getClosestCollision().getGeometry().getParent().getName();
            float roadDirection[] = results.getClosestCollision().getGeometry().getParent().getLocalRotation().toAngles(null);
            //System.out.println(roadDirection[0] + " " + roadDirection[1] + " " + roadDirection[2]);
            roadDirection[0] += FastMath.PI / 2f;

            /** BEGIN TEMPORARY DIRECTION CODE**/          
            Vector3f roadDir = new Quaternion().fromAngles(roadDirection).getRotationColumn(0),
                     oppositeRoadDir = roadDir.negate(),
                     camDir = dir;
            /*System.out.println(roadDir);
            System.out.println(camDir);
            float temp = camDir.getX();
            camDir.setX(camDir.getZ());
            camDir.setZ(temp);*/
            
            
            if (camDir.distance(oppositeRoadDir) < camDir.distance(roadDir)) {
                onRoad = false;
            }
            else {
                onRoad = true;
            }         
            /** END TEMPORARY DIRECTION CODE**/
            
            //System.out.println(NodeInspector.getFullRoadName(worldNode, streetInfoMap, "NorthAve"));
            
            if (roadObj.startsWith("R_")) {
                if (streetInfoMap.containsKey(roadObj)){
                    currentStreet = streetInfoMap.get(roadObj).getStreetName();
                    if (onRoad) {
                        nextStreet = "Next: " + streetInfoMap.get(roadObj).getNextStreetName();
                    }
                    else {
                        nextStreet = "Next: " + streetInfoMap.get(roadObj).getPrevStreetName();
                    }               
                } else {
                    currentStreet = nextStreet = "";
                }
                 
            } else if (roadObj.startsWith("X_")) {
                
                if (intersectionInfoMap.containsKey(roadObj.substring(2)) && intersectionInfoMap.get(roadObj.substring(2)).getRoadIntersections() != null) {
                    Set<String> roadNames = intersectionInfoMap.get(roadObj.substring(2)).getRoadIntersections().keySet();
                    String intersectionName = "";
                    
                    if (!roadNames.isEmpty()) {
                        intersectionName = roadNames.toString().replace("[", "").replace("]", "").replace(", ", "/");

                        if (nifty.getCurrentScreen() != null && nifty.getCurrentScreen().getScreenId().equals("hud")){     
                            currentStreet = intersectionName;
                            nextStreet = ""; 
                        }
                    }
                }
                
                
            }
            
            /*if (!nifty.getCurrentScreen().isNull() && nifty.getCurrentScreen().getScreenId().equals("hud")){           
                nifty.getCurrentScreen().findNiftyControl("currentStreet", Label.class).setText(currentStreet);
                
                nifty.getCurrentScreen().findNiftyControl("currentTime", Label.class).setText(currentTime);
            }*/
        }
        else{
            currentStreet = "";
            nextStreet = "";
        } 
        
        if (nifty.getCurrentScreen() != null && nifty.getCurrentScreen().getScreenId().equals("hud")){
            nifty.getCurrentScreen().findNiftyControl("currentStreet", Label.class).setText(currentStreet);
            nifty.getCurrentScreen().findNiftyControl("nextStreet", Label.class).setText(nextStreet);
            nifty.getCurrentScreen().findNiftyControl("currentTime", Label.class).setText(currentTime);                              
            nifty.getCurrentScreen().findNiftyControl("currentSpeedKPH", Label.class).setText(Integer.toString((int)CityHelper.mstokph(car.getCombustionPropUnit().getSpeed())));                
        }
    }

    public AppSettings getSettings() {
        return settings;
    }

    public void setSettings(AppSettings settings) {
        this.settings = settings;
    }

    public String getCurrentStreet() {
        return currentStreet;
    }

    public void setCurrentStreet(String currentStreet) {
        this.currentStreet = currentStreet;
    }

    public Screen getHudScreen() {
        return hudScreen;
    }

    public void setHudScreen(Screen hudScreen) {
        this.hudScreen = hudScreen;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }
    
    public String getNextStreet() {
        return nextStreet;
    }

    public void setNextStreet(String nextStreet) {
        this.nextStreet = nextStreet;
    }

    public boolean isOnRoad() {
        return onRoad;
    }

    public void setOnRoad(boolean onRoad) {
        this.onRoad = onRoad;
    }
}