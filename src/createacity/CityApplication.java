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
package createacity;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
import createacity.states.*;

/**
 * <code>createacityApplication</code> extends the {@link com.jme3.app.SimpleApplication}
 * class to provide default functionality like a first-person camera,
 * and an accessible root node that is updated and rendered regularly, 
 * as well as features unique to createacity such as saving and loading.
 * Additionally, <code>createacityApplication</code> will display a statistics view
 * using the {@link com.jme3.app.StatsView} class. It will display
 * the current frames-per-second value on-screen in addition to the statistics.
 * Several keys have special functionality in <code>createacityApplication</code>:<br/>
 *
 * <table>
 * <tr><td>C</td><td>- Display the camera position and rotation in the console.</td></tr>
 * <tr><td>M</td><td>- Display memory usage in the console.</td></tr>
 * <tr><td>F5</td><td>- Show/hide fps and other useful debugging info.</td></tr>
 * </table>
 */
public class CityApplication extends SimpleApplication{
        
    public MainState mainState;
    protected Nifty nifty;
    public static final boolean DEBUG = false;
    
    @Override
    public void initialize(){  
        super.initialize();
        
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
        nifty = niftyDisplay.getNifty();
        guiViewPort.addProcessor(niftyDisplay);
        
        if (inputManager.hasMapping(INPUT_MAPPING_EXIT)) {
            inputManager.deleteMapping(INPUT_MAPPING_EXIT);
        }
        
        setDisplayFps(false);
        setDisplayStatView(false);
        mainState = new DrivingState(this);
         
        viewPort.detachScene(rootNode);
        viewPort.attachScene(mainState.getRootNode());
        stateManager.attach(mainState);  
        
        setupKeys();    
    }
    
    private void setupKeys() {
        inputManager.addMapping("Teleport to Location", new KeyTrigger(KeyInput.KEY_T));
        inputManager.addMapping("Place Box", new KeyTrigger(KeyInput.KEY_B));
        inputManager.addMapping("Find Nearest Street", new KeyTrigger(KeyInput.KEY_F));
        
        inputManager.addListener(actionListener, "Teleport to Location", "Place Box", "Find Nearest Street");
    }
    
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String binding, boolean keyPressed, float tpf){
            if(binding.equals("Teleport to Location") && keyPressed){
                CityHelper.moveCamera(cam);
            }
            
            if(binding.equals("Place Box") && keyPressed){
                CityHelper.placeBox(mainState.getRootNode(), assetManager);
            }
            
            if(binding.equals("Find Nearest Street") && keyPressed){
                MainState.findingClosestRoad = true;
            }
        }
    };
    
    public Nifty getNifty(){
        return nifty;
    }
    
    public AppSettings getSettings(){
        return settings;
    }

    @Override
    public void simpleInitApp() {
       
    }   
}