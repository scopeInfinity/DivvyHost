package divvyhost.configuration;

import divvyhost.utils.Paths;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author scopeinfinity
 */
public class Configuration {
    private static final Logger log = Logger.getLogger(Configuration.class.getName());
    
    public static int PORT_RPC      = 1099;
    public static int PORT_FAST     = 10404;
    public static int PORT_SERVICE  = 10405;
    public static int WEB_PORT      = 9999; 
    
    public static int PORT_SCAN_TIMEOUT = 5000;
    public static int CLIENT_CONNECT_TIMEOUT = 5000;
    
    public static long SCHEDULER_REFRESH_TIMER  = 15*60*1000;
    
    public static boolean AUTO_EXPORTPROJECT_ONLOAD = false;
    
    public static int BUFFER_SIZE_SERVER1 = 20*1024*1024;
    public static int BUFFER_SIZE_SERVER2 = 20*1024*1024;
    public static int BUFFER_SIZE_CLIENT1 = 20*1024*1024;
    public static int BUFFER_SIZE_CLIENT2 = 20*1024*1024;
    
    public static boolean fastScanEnabled           = true;
    public static boolean fastScanServerEnabled     = true;
    public static String FAST_SCAN_MESSAGE          = "[[DivvyHost]]";
    
    public static String SIGN_ALGO = "DSA";
    public static int SIGN_KEYSIZE  = 1024; 
    
    private static final String CONF_FILENAME = "conf.properties";
    
    private static final String CONF_INTERAL_IP = "INTERNAL_IP";
    private static final String CONF_MAX_SIZE_DISK = "MAX_SIZE_ON_DISK_MB";
    
    private boolean isLoadedFine;
    
    private List<Integer> prefixLength;
    private List<InetAddress> internalIPs;
    
    public static int MAX_PROJECT_ALLOWED_SIZE = 10*1024*1024;
    private int maxSizeAllowedOnDisk;
    
    /**
     * Load Configuration from File
     */
    public Configuration() {
        Paths paths = new Paths();
        File conf = new File(paths.getConfDir(),CONF_FILENAME);
        prefixLength = new ArrayList();
        internalIPs = new ArrayList<>();
        if(!conf.exists()) {
            log.info("Configuration not Found!");
            createDefaultConfiguration(conf);
        }
        
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
            maxSizeAllowedOnDisk = 200;
            String IPs = properties.getProperty(CONF_INTERAL_IP); 
            String[] IPList = IPs.split(",");
            for(String IP : IPList) {
                try{
                    int index = IP.indexOf("/");
                    InetAddress address = InetAddress.getByName(IP.substring(0,index).trim());
                    Integer prefix = Integer.parseInt(IP.substring(index+1).trim());
                    internalIPs.add(address);
                    prefixLength.add(prefix);
                }catch(Exception e){
                    log.severe("Ignored "+CONF_INTERAL_IP+" : "+IP);
                }
            }
            if(internalIPs.size() == 0) {
                internalIPs.add(InetAddress.getByName("127.0.0.1"));
                prefixLength.add(31);
            
            }
            try{
                maxSizeAllowedOnDisk = Integer.parseInt(properties.getProperty(CONF_MAX_SIZE_DISK));
            }catch(Exception e){
                log.severe("Ignored "+CONF_MAX_SIZE_DISK);
            }
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
    
    private void createDefaultConfiguration(File file) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            Properties properties = new Properties();
            properties.put(CONF_INTERAL_IP, "172.16.156.0/24,127.0.0.1/31");
            properties.put(CONF_MAX_SIZE_DISK, String.valueOf(200));
            properties.save(os, "Divvy Host Configuration" );
            os.flush();
            log.info("Default Configuration Created");
            
        } catch (FileNotFoundException ex) {
            log.severe(ex.toString());
        } catch (IOException ex) {
            log.severe(ex.toString());
        } finally {
            try {
                if(os!=null)
                    os.close();
            } catch (IOException ex) {
                log.severe(ex.toString());
            }
        }
    }

    public boolean isLoadedFine() {
        if(isLoadedFine) {
            for (int i = 0; i < internalIPs.size(); i++) {
                log.info(CONF_INTERAL_IP + " = " + internalIPs.get(i) + "/" + prefixLength.get(i) );
            }
            log.info(CONF_MAX_SIZE_DISK + " = " + maxSizeAllowedOnDisk + "MB" );
        }
        return isLoadedFine;
    }

    public List<InetAddress> getInternalIPs() {
        return internalIPs;
    }

    public List<Integer> getPrefixLengths() {
        return prefixLength;
    }

    public long getMaxSizeAllowedOnDisk() {
        return maxSizeAllowedOnDisk*1024L*1024;
    }
}
