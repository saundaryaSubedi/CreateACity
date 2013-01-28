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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Supports updating to the latest stable version of createacity. Checks a download
 * URL and compares the latest version to the installed version.
 * @author WumpaFruit
 */
public class UpdateManager {

    /**
     * Status of the installed version of createacity.
     */
    public static enum Status {

        /**
         * The installed version of createacity is current and up-to-date.
         */
        CURRENT,
        /**
         * There is a newer stable version of createacity available.
         */
        NOT_CURRENT,
        /**
         * The URL to check for a newer version of createacity could not be found.
         */
        URL_NOT_FOUND,
        /**
         * There was some other failure in checking for a newer version.
         */
        FAILURE
    };
    
    /**
     * The update URL that lists the newest stable version of createacity.
     */
    public static String updateURL = "http://playcreateacity.com/createacity/update.nyc";
    /**
     * The installed game version.
     */
    public static String gameVersion = "1.0";
    /**
     * The installed game modifier.
     */
    public static String modifier = "Hatchling";
    /**
     * The latest game version.
     */
    public static String latestGameVersion = "Need to call checkUpdate()!";
    /**
     * The latest game modifier.
     */
    public static String latestModifier = "Need to call checkUpdate()!";
    /**
     * The URL to download the latest version.
     */
    public static String downloadURL = "Need to call checkUpdate()!";

    /**
     * Updates the status of the installed game by checking for the newest
     * version of createacity.
     * @return the status of the installation
     */
    public static Status checkUpdate(){
        Status updateStatus = Status.FAILURE;
        URL url;
        InputStream is;
        InputStreamReader isr;
        BufferedReader r;
        String line;

        try{
            url = new URL(updateURL);
            is = url.openStream();
            isr = new InputStreamReader(is);
            r = new BufferedReader(isr);
            String variable, value;

            while((line = r.readLine()) != null){
                if (!line.equals("") && line.charAt(0) != '/'){
                    variable = line.substring(0, line.indexOf('='));
                    value = line.substring(line.indexOf('=') + 1);

                    if (variable.equals("Latest Version")){
                        variable = value;
                        value = variable.substring(0, variable.indexOf(" "));
                        variable = variable.substring(variable.indexOf(" ") + 1);

                        latestGameVersion = value;
                        latestModifier = variable;

                        if (Float.parseFloat(value) > Float.parseFloat(gameVersion))
                            updateStatus = Status.NOT_CURRENT;
                        else
                            updateStatus = Status.CURRENT;

                    }
                    else if (variable.equals("Download URL"))
                        downloadURL = value;

                }
            }

            return updateStatus;
            
        } catch(MalformedURLException e){
            return Status.URL_NOT_FOUND;
        } catch(IOException e){
            return Status.FAILURE;
        }
        
    }

}
