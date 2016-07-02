package divvyhost.project;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.logging.Logger;
import divvyhost.utils.Paths;
import divvyhost.utils.Utils;

/**
 *
 * @author scopeinfinity
 */
public class Data implements Serializable{
    private static final Logger log = Logger.getLogger(Data.class.getName());
    
    private String title, description;
    private byte[] data;
    

    public Data(String title, String description, byte[] data) {
        this.title = title;
        this.description = description;
        this.data = data;
    }

    public Data(String title, String description) {
        this.title = title;
        this.description = description;
        this.data = null;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public byte[] getData() {
        return data;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }
    
    /**
     * Export Project Data to required Directory
     * @param projectDirectory
     * @param publicKey
     * @return 
     */
    public boolean exportData(String projectDirectory) {
        File dirExport = (new Paths()).getHostDir();
        File dirProject = new File(dirExport, projectDirectory);
        dirProject.mkdirs();
        if(Utils.unzip(new ByteArrayInputStream(data), dirProject))  
            log.info(projectDirectory+" Exported");
        else {
            log.severe(projectDirectory+" Export Failed!!!");
            return false;
        }
        return true;
    }
    
    public boolean importData(String projectDirectory) {
        File dirExport = (new Paths()).getHostDir();
        File dirProject = new File(dirExport, projectDirectory);
        if (!(dirProject.isDirectory() && dirProject.exists()) ) {
            log.severe("["+projectDirectory+"] Directory Not Exists");
            return false;
        }
        byte[] data = Utils.zip(dirProject);
        if(data!=null)  {
            this.data = data;
            log.info(projectDirectory+" Imported(Just Data)");
            return true;
        }
        else {
            log.severe(projectDirectory+" Import Failed!!!");
        }
        return false;
      
    }
}
