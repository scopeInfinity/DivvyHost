package divvyhost.configuration;

/**
 *
 * @author scopeinfinity
 */
public class Configuration {
    public static int PORT_TCP      = 10403;
    public static int PORT_UCP      = 10404;
    public static int WEB_PORT      = 9999; 
    
    public static int PORT_SCAN_TIMEOUT = 5000;
    public static int CLIENT_CONNECT_TIMEOUT = 5000;
    
    public static long SCHEDULER_REFRESH_TIMER  = 1*60*1000;
    
    public static boolean AUTO_EXPORTPROJECT_ONLOAD = true;
    
    public static String SIGN_ALGO = "DSA";
    public static int SIGN_KEYSIZE  = 1024; 
    
}
