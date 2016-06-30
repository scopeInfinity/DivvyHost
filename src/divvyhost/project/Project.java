package divvyhost.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import divvyhost.utils.Paths;

/**
 *
 * @author scopeinfinity
 */
public class Project implements Serializable{
    private static final Logger log = Logger.getLogger(Project.class.getName());
    
    private Details details;
    private Data data;
    private byte[] publicKey;
    
    /**
     * Obtain Project Instance from file
     * @param file
     * @return projectObject
     */
    public static Project getInstance(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Project project = (Project) ois.readObject();
            ois.close();
            return project;
        } catch (FileNotFoundException ex) {
           log.severe(ex.toString());
        } catch (IOException ex) {
           log.severe(ex.toString());
        } catch (ClassNotFoundException ex) {
            log.severe(ex.toString());
            log.severe("Invalid/Outdated File");
        }
        return null;
    }
    
    /**
     * Return Project using, String(UUID)
     * @param uId
     * @return projectObject
     */
    public static Project load(String uId) {
        File dirSave = (new Paths()).getProjectsDir();
        File file = new File(dirSave, uId);
        return getInstance(file);
    }
    
    /**
     * Save Project, details + WholeData + publicKey
     * @return isSaved
     */
    public boolean save() {
        File dirSave = (new Paths()).getProjectsDir();
        File file = new File(dirSave, details.getFileName());
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            return true;
        } catch (FileNotFoundException ex) {
            log.severe(ex.toString());
        } catch (IOException ex) {
            log.severe(ex.toString());
        }
        return false;
    }
    
    /**
     * Export Current Project to Required Place
     * @return isExported
     */
    public boolean export() {
        if (data == null)
        {
            log.severe(details.getFileName()+" Data Not Avaible for Export!");
            return false;
        }
        return data.export(details.getFileName(), publicKey);
    }
}
