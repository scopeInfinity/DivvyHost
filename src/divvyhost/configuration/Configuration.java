package divvyhost.configuration;

import divvyhost.utils.Paths;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author scopeinfinity
 */
public class Configuration {
    private static final Logger log = Logger.getLogger(Paths.class.getName());
    
    public static int PORT_TCP      = 10403;
    public static int PORT_FAST      = 10404;
    public static int WEB_PORT      = 9999; 
    
    public static int PORT_SCAN_TIMEOUT = 5000;
    public static int CLIENT_CONNECT_TIMEOUT = 5000;
    
    public static long SCHEDULER_REFRESH_TIMER  = 1*60*1000;
    
    public static boolean AUTO_EXPORTPROJECT_ONLOAD = false;
    
    public static int BUFFER_SIZE_SERVER1 = 10*1024*1024;
    public static int BUFFER_SIZE_SERVER2 = 10*1024*1024;
    public static int BUFFER_SIZE_CLIENT1 = 10*1024*1024;
    public static int BUFFER_SIZE_CLIENT2 = 10*1024*1024;
    
    public static boolean fastScanEnabled           = false;
    public static boolean fastScanServerEnabled     = false;
    public static String FAST_SCAN_MESSAGE          = "[[DivvyHost]]";
    
    public static String SIGN_ALGO = "DSA";
    public static int SIGN_KEYSIZE  = 1024; 
    
    private static final String CONF_FILENAME = "conf.properties";
    
    private static final String CONF_INTERAL_IP = "INTERNAL_IP";
    private static final String CONF_PREFIX_LENGTH = "PREFIX_LENGTH";
    
    private boolean isLoadedFine;
    
    private int prefixLength;
    private InetAddress internalIP;
    
    /**
     * Load Configuration from File
     */
    public Configuration() {
        Paths paths = new Paths();
        File conf = new File(paths.getConfDir(),CONF_FILENAME);
        isLoadedFine = loadConfiguration(conf);
    }
    
    /**
     * Read Configurations
     * @param file 
     */
    private boolean loadConfiguration(File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(fis);
            internalIP = InetAddress.getByName(properties.getProperty(CONF_INTERAL_IP));
            prefixLength = Integer.parseInt(properties.getProperty(CONF_PREFIX_LENGTH));
            fis.close();
            return true;
            
        } catch (FileNotFoundException ex) {
            log.severe("Configuration file not found!");
        } catch (Exception ex) {
            log.severe(ex.toString());
        } finally {
            try {
                if(fis!=null)
                    fis.close();
            } catch (Exception ex) {
               log.severe(ex.toString());
            }
        }
        return false;
    }

    public boolean isLoadedFine() {
        if(isLoadedFine) {
            log.info(CONF_INTERAL_IP + " = " + internalIP );
            log.info(CONF_PREFIX_LENGTH + " = " + prefixLength );
        }
        return isLoadedFine;
    }

    public InetAddress getInternalIP() {
        return internalIP;
    }

    public int getPrefixLength() {
        return prefixLength;
    }

}
