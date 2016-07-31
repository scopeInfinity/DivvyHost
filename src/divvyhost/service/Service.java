package divvyhost.service;

import divvyhost.DivvyHost;
import divvyhost.network.DivvyClient;
import java.util.logging.Logger;

/**
 *
 * @author scopeinfinity
 */
public class Service {
    private static final Logger log = Logger.getLogger(Service.class.getName());
    
    public static String MESSAGE_START_GUI  = "RUN_GUI";
    public static String MESSAGE_STOP_GUI   = "STOP_GUI";
    
    private Client client;
    private Server server; 
    
    public Service() {
        server = new Server();
        client = new Client();
    }
    
    public boolean start(String flag) {
        if(client.connect())
        {
            log.info("Another DivvyHost Instance is Running");
            if(flag!=null) {
                if(flag.equals("startgui"))
                    client.startGUI();
                else if(flag.equals("stopgui"))
                    client.stopGUI();
                else if(flag.equals("test"))
                    log.info("Test : Another Instance is Running");
                else
                    log.severe("Invalid Service Flag, Ignored");
            }
            return false;
        }
        else
        {
            log.info("DivvyHost Main Is going to Run");
            if(flag!=null && flag.equals("test")){
                log.info("Test : No Another DivvyHost Instance is Running");
                return false;
            } else if(flag!=null)
                log.severe("DivvyHost Main Instance, Service Flag Not Supported");
           
            if(!server.start()) 
                log.severe("DivvyHost Server Service Failed!!!");
            return true;
        }
    }

    public void setDivvyHost(DivvyHost divvyHost) {
        server.setDivvyHost(divvyHost);
    }
    
    
}
