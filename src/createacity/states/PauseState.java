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

package createacity.states;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import createacity.CityApplication;

public class PauseState extends AbstractAppState implements ActionListener{
    private CityApplication app;
    
    @Override
    public void initialize(AppStateManager stateManager, Application app){
        super.initialize(stateManager, app);
        this.app = (CityApplication) app;
        initKeys();
    }
    
    private void initKeys(){
        InputManager inputManager = app.getInputManager();
        
        inputManager.addMapping("PAUSE_Return", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addMapping("PAUSE_Quit", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(this, "PAUSE_Return", "PAUSE_Quit");
    }

    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("PAUSE_Quit"))
            app.stop();
        else if (name.equals("PAUSE_Return") && isPressed){
            
            app.getStateManager().detach(this);
            app.getStateManager().attach(app.mainState);
            //app.getViewPort().attachScene(mainState.getRootNode());
        }
    }
    
    @Override
    public void stateAttached(AppStateManager stateManager) {
        if (isInitialized())
            initKeys();
        
    }
    
    @Override
    public void stateDetached(AppStateManager stateManager) {
        app.getInputManager().clearMappings();
    }
    
    /*@Override
    public void cleanup(){
        super.cleanup();
        app.getInputManager().deleteMapping("PAUSE_Return");
        app.getInputManager().deleteMapping("PAUSE_Quit");
        app.getInputManager().removeListener(this);
        app.getInputManager().addMapping(createacityApplication.INPUT_MAPPING_PAUSE, new KeyTrigger(KeyInput.KEY_ESCAPE));
    }*/
}
