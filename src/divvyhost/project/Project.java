package divvyhost.project;

import divvyhost.DivvyHost;
import divvyhost.users.User;
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
import divvyhost.utils.Utils;
import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

/**
 *
 * @author scopeinfinity
 */
public class Project implements Serializable{
    private static final Logger log = Logger.getLogger(Project.class.getName());
    private static final long serialVersionUID = 2599785789517212065L;
            
    private byte[] publicKey;
    private Details details;
    private Data data;
    private byte[] signature;

    /**
     * Kryo Serialization
     */
    public Project() {
    }

    /**
     * Create A fresh Project, need to call GenerateSign
     * @param publicKey
     * @param details
     * @param data
     * @param signature 
     */
    public Project(PublicKey publicKey, Details details, Data data) {
        this.publicKey = publicKey.getEncoded();
        this.details = details;
        this.data = data;
    }

    public Project(PublicKey publicKey, Details details, Data data, byte[] signature) {
        this.publicKey = publicKey.getEncoded();
        this.details = details;
        this.data = data;
        this.signature = signature;
    }

    @Override
    public String toString() {
        if (data == null)
            return details.getpID().toString();
        return data.getTitle();
    }
    
    
    public File createHostPath() {
        Paths path = new Paths();
        File dir = new File(path.getHostDir(), details.getFileName());
        dir.mkdirs();
        if(dir.exists() && dir.isDirectory())
        {
            log.info(dir+" Project Host Directory Created");
            return dir;
        } else {    
            log.severe(dir+" Project Host Directory Creation Failed!!");
        }
        return null;
    }
    
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
            if (project == null || !project.completeValidation()) {
                log.severe("Project not Loaded or Complete Validation Failed!!");
                return null;
            }
            
            return project;
        } catch (FileNotFoundException ex) {
           log.severe(ex.toString());
        } catch (IOException ex) {
           log.severe(ex.toString());
        } catch (ClassNotFoundException ex) {
            log.severe(ex.toString());
            log.severe("Outdated File");
        } catch (Exception ex) {
            log.severe(ex.toString());
            log.severe("Invalid File");
        } 
        return null;
    }
    
    public boolean completeValidation() {
        if(!isValid()) {
            log.severe("Project Load Failed!!");
            return false;
        }
        if (!signValidate()) {
            log.severe("Loaded Project "+getDetails().getFileName()+" : Signature Verify Failed!!");
            return false;
        }
        if (getDetails().getpID()==null) {
            log.severe("Load Failed : Project "+getDetails().getFileName()+" : pID not Available");
            return false;
        }
        if (!getDetails().getpID().equals(getData().getpID())) {
            log.severe("Load Failed : Project "+getDetails().getFileName()+" : pID Forged!!");
            return false;
        }
        return true;
    }
    
    /**
     * Basic Value Null Check
     * @return isValid
     */
    public boolean isValid() {
        if (getDetails()==null) {
           log.severe("Loaded Project : Incomplete (No Detials)");
           return false;
        }
        if (getData()==null) {
           log.severe("Loaded Project "+details.getFileName()+" : Incomplete (No Data)");
           return false;
        }
        if (getPublicKey()==null) {
           log.severe("Loaded Project "+details.getFileName()+" : Incomplete (No Public Key)");
           return false;
        }
        if (getSignature()==null) {
           log.severe("Loaded Project "+details.getFileName()+" : Incomplete (No Signature)");
           return false;
        }
        return true;
    }
    
    /**
     * Validate Signature
     * @return isDataSignedGood
     */
    private boolean signValidate() {
        ObjectOutputStream oos = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(data);
            return Utils.verifyData(baos.toByteArray(), signature, getPublicKey());
        } catch (IOException ex) {
            log.severe(ex.toString());
        } finally {
            try {
                oos.close();
            } catch (IOException ex) {
               log.severe(ex.toString());
            }
        }
        return false;
    }
    
    /**
     * Generate Sign of Data
     * @return isSigned
     */
    private boolean generateSign(PrivateKey privateKey) {
        ObjectOutputStream oos = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(data);
            byte[] signature = Utils.signData(baos.toByteArray(), privateKey);
            if(signature == null)
                log.severe("Signing Failed : "+details.getFileName());
            else
            {
                this.signature = signature;
                return true;
            }
        } catch (IOException ex) {
            log.severe(ex.toString());
        } finally {
            try {
                oos.close();
            } catch (IOException ex) {
               log.severe(ex.toString());
            }
        }
        return false;
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
     * Check if Space is Available
     * @return isSaved
     */
    public boolean save() {
        File dirSave = (new Paths()).getProjectsDir();
        File file = new File(dirSave, details.getFileName());
        long ignoreSize = 0;
        if (file.exists())
            ignoreSize = file.length();
        
        if (!DivvyHost.canSaveMoreProject(ignoreSize)) {
            if(isCurrentUserOwner()) {
                log.severe(" MEMORY EXCEEDING, Project of Current User only! Saving...");
                return true;
            }
            log.severe(">>>MEMORY EXCEEDING, Not Saving The Project");
            return false;
        }
        
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
    public boolean exportProject() {
        if (data == null) {
            log.severe(details.getFileName()+" Data Not Available for Export!");
            return false;
        }
        if (signValidate())
            return data.exportData(details.getFileName());
        else {
            log.severe("Project ["+getDetails().getpID()+"] Signing Verify Failed!");
            return false;
        }
    }
    
    /**
     * 
     * @param title, If null then No Change
     * @param desciption, If null then No Change
     * @param privateKey
     * @return isImported
     */
    public boolean importProject(String author, String title, String desciption, PrivateKey privateKey) {
        
        if (!isCurrentUserOwner()) {
            log.severe("Project "+getDetails().getpID()+ " is Owned By Different User");
            return false;
        }
        
        if (author==null && this.data!=null)
            author = this.data.getAuthor();
        if (title==null && this.data!=null)
            title = this.data.getTitle();
        if (desciption==null && this.data!=null)
            desciption = this.data.getDescription();
        
        Data data = new Data(details.getpID(), author, title, desciption, null);
        if (data.importData(details.getFileName()) )
        {
            Data olddata = this.data;
            this.data = data;
            if (generateSign(privateKey)) {
                details.setLastModified();
                log.info("["+details.getFileName()+"] Project Imported");
                return true;
            } else {
                this.data = olddata;
                log.severe("["+details.getFileName()+"] Signing Failed");
            }
        }
        else {
            log.severe("["+details.getFileName()+"] Project Failed");
        }
        return false;
    }

    public Data getData() {
        return data;
    }

    public Details getDetails() {
        return details;
    }
    
    public PublicKey getPublicKey() {
        PublicKey key = Utils.getDSAPublicKey(publicKey);
        if (key == null) 
            log.severe("Public Key Retrive Failed : "+getDetails().getpID());
        return key;
    }

    public byte[] getSignature() {
        return signature;
    }
    
    public String getUser() {
        return Utils.getMD5(publicKey);
    }

    boolean autoExportProject() {
        return exportProject();
    }
    
    /**
     * Check if this Project is Owned by Current User
     * @return isCurrentUserOwner()
     */
    private boolean isCurrentUserOwner() {
        User user = User.loadUser();
        if (user.getPublicKey().equals(getPublicKey()))
            return true;
        return false;
    }
}
