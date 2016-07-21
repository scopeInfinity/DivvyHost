package divvyhost.users;

import divvyhost.project.Project;
import divvyhost.utils.Pair;
import divvyhost.utils.Paths;
import divvyhost.utils.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author scopeinfinity
 */
public class User implements Serializable {
    private static final Logger log = Logger.getLogger(User.class.getName());
    private static final long serialVersionUID = 5966745447360552510L;
    
    private static String filename   =  "user.dat";
    
    private static User instance;
    
    private PrivateKey privatekey;
    private PublicKey publicKey;
    private String user;
    
    
    public User(PrivateKey privatekey, PublicKey publicKey, String user) {
        this.privatekey = privatekey;
        this.publicKey = publicKey;
        this.user = user;
    }
    
    public static User newUser() {
        Pair<PublicKey, PrivateKey> keys = Utils.generateDSAKeys();
        User user = new User(keys.getSecond(), keys.getFirst(), Utils.getMD5(keys.getFirst().getEncoded()));
        return user;
    }
    
    private static File getUserFile() {
        Paths path = new Paths();
        File rootDir = path.getRootDir();
        File file = new File(rootDir, filename);
        return file;
    }
    
    // Designed for Single User
    
    public static boolean isUserExists() {
        File file = getUserFile();
        if(file!=null)
            if(file.exists())
                return true;
        return false;
    }
    
    /**
     * Load User from File
     * @return User
     */
    public static User loadUser() {
        if (instance != null) 
            return instance;
        
        if (!isUserExists()) {
            log.info("User Not Exists\nCreating User");
            User user = newUser();
            log.info("New User : "+user.getUser());
            user.save();
            return instance = user;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(getUserFile());
            ObjectInputStream ois = new ObjectInputStream(fis);
            User user = (User) ois.readObject();
            ois.close();
            log.info("User Loaded : "+user.getUser());
            return instance = user;
            
        } catch (FileNotFoundException ex) {
            log.severe(ex.toString());
        } catch (IOException ex) {
            log.severe(ex.toString());
        } catch (ClassNotFoundException ex) {
            log.severe(ex.toString());
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                log.severe(ex.toString());
            }
        }
        
        log.info("User Load Error!");
        return null;
        
    }
    
    /**
     * Save User to file
     * @return 
     */
    public boolean save() {
       try {
           FileOutputStream fos = new FileOutputStream(getUserFile());
           ObjectOutputStream oos = new ObjectOutputStream(fos);
           oos.writeObject(this);
           oos.close();
           log.info("User Saved : "+getUser());
           return true;
       } catch (FileNotFoundException ex) {
           log.severe(ex.toString());
       } catch (IOException ex) {
           log.severe(ex.toString());
       }
       log.info("User Save Error!");
       return false;
    }
    

    public PrivateKey getPrivatekey() {
        return privatekey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getUser() {
        return user;
    }
    
    
}
