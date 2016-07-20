package divvyhost.project;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.logging.Logger;
import divvyhost.utils.Paths;
import divvyhost.utils.Utils;
import java.util.UUID;

/**
 *
 * @author scopeinfinity
 */
public class Data implements Serializable{
    private static final Logger log = Logger.getLogger(Data.class.getName());
    private static final long serialVersionUID = 3138851497398476374L;
    
    private String title, description;
    private byte[] data;
    private String author;
    
    // Project ID for Cross Verification
    private String pID;

    /**
     * Kryo Serialization
     */
    public Data() {
    }
    
    public Data(UUID pID, String author, String title, String description, byte[] data) {
        this.title = title;
        this.description = description;
        this.data = data;
        this.author = author;
        this.pID = pID.toString();
    }

    public Data(UUID pID, String author, String title, String description) {
        this.title = title;
        this.description = description;
        this.data = null;
        this.author = author;
        this.pID = pID.toString();
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public UUID getpID() {
        return UUID.fromString(pID);
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

    public String getAuthor() {
        return author;
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
        Utils.emptyDirectory(dirProject);
        dirProject.mkdirs();
        
        if (data == null) {
            log.severe("No Data Avaliable to Export!");
            return false;
        }
        
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
