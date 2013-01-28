package createacity.info;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import java.io.IOException;

/**
 * Contains information about a road object
 * @author Joseph
 */
public class RoadInfo implements Savable{
    private String borough;
    private String name;

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(borough, "borough", "");
        capsule.write(name, "name", "");
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule capsule = im.getCapsule(this);
        borough = capsule.readString("borough", "");
        name = capsule.readString("name", "");
    }
    
}
