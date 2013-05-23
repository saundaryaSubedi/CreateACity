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

import com.jme3.system.AppSettings;

/**
 * Client code for createacity
 */
public class CreateACity{
    
    public static void main(String[] args) {
        CityApplication app = new CityApplication();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Create a City Demo");
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setUseJoysticks(true);
        app.setSettings(settings);
        app.start();
    }
}