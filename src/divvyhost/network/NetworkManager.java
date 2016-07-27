package divvyhost.network;

import divvyhost.project.ProjectManager;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author scopeinfinity
 */
public class NetworkManager {
    private static final Logger log = Logger.getLogger(NetworkManager.class.getName());
    
    private DivvyServer divvyServer;
    private ProjectManager projectManager; 
    private String user;
    
    public NetworkManager(ProjectManager projectManager, String user) {
        this.projectManager = projectManager;
        this.user = user;
        divvyServer = new DivvyServer(projectManager, user);
        if (!divvyServer.start()) {
            log.severe("SERVER CANNOT BE STARTED!!!!!!!!!");
        }
    }
    static int counter = 0;
    /**
     * Start Syncing Current Project with others
     * Currently Checking all Possible Server in One Go
     */
    void startSync() {
        DivvyClient divvyClient = new DivvyClient(projectManager, user);
        while(true) {
            if (!divvyClient.scanNetwork()) {
                log.info("No other Server Found on Network");
                continue;
            }
            
            log.info("Last Server freshOne : "+divvyClient.isLastFreshServer());
            if (!divvyClient.isLastFreshServer()) {
                divvyClient.makeServerHistoryClear();
                return;
            }
            
            if (!divvyClient.connect()) {
                log.severe("Unable to connect to client");
                continue;
            }
            else {
                divvyClient.sync();
            }
            divvyClient.disconnect();
            try {
                Thread.sleep(100);
                System.out.println("Sleep Server"+ ++counter);
                System.out.flush();
                System.gc();
            } catch (Exception ex) {
                Logger.getLogger(NetworkManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public ProjectManager getProjectManager() {
        return projectManager;
    }
    
    
    
}
