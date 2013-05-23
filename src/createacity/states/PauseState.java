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
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PauseState extends AbstractAppState implements ActionListener, ScreenController{
    private CityApplication app;
    private Screen screen;
    private Nifty nifty;
    
    public PauseState() {}
    
    public PauseState(CityApplication app) {
        this.app = app;
        this.nifty = app.getNifty();
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app){
        super.initialize(stateManager, app);
        this.app = (CityApplication) app;
        nifty = ((CityApplication)app).getNifty();
        screen = nifty.getCurrentScreen();
    }
    
    private void addInputMappings(){
        InputManager inputManager = app.getInputManager();
        
        inputManager.addMapping("PAUSE_Return", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addMapping("PAUSE_Quit", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(this, "PAUSE_Return", "PAUSE_Quit");
    }
    
    private void removeInputMappings() {
        InputManager inputManager = app.getInputManager();
        
        if (inputManager.hasMapping("PAUSE_Quit")) {
            inputManager.deleteMapping("PAUSE_Quit");
        }
        
        if (inputManager.hasMapping("PAUSE_Return")) {
            inputManager.deleteMapping("PAUSE_Return");
        }
        
        inputManager.removeListener(this);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("PAUSE_Quit")) {
            exit();
        }
        else if (name.equals("PAUSE_Return") && isPressed){
            resume();
        }
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        
        if (enabled) {
            addInputMappings();
            nifty.fromXml("pause.xml", "pause", this);
        } else {
            removeInputMappings();
            
            nifty.removeScreen("pause");
        }
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }

    @Override
    public void onStartScreen() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onEndScreen() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void update(float tpf) {
        
    }
  
    public void resume() {
        setEnabled(false);
        app.getMainState().setEnabled(true);
    }
    
    public void exit() {
        nifty.fromXml("pause.xml", "leaveFeedback", this);
        //app.stop();
    }
    
    public void leaveFeedback() {
        try {
            BrowserLauncher launcher = new BrowserLauncher();
            launcher.openURLinBrowser("http://createacity.org/2013/05/demo-now-available/");
        } catch (BrowserLaunchingInitializingException | UnsupportedOperatingSystemException ex) {
            Logger.getLogger(PauseState.class.getName()).log(Level.SEVERE, null, ex);
        }
        app.stop();
    }
    
    public void reallyExit() {
        app.stop();
    }
}
