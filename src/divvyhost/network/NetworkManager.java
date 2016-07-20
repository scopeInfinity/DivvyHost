package divvyhost.network;

import divvyhost.project.ProjectManager;
import java.util.UUID;
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
    
    /**
     * Start Syncing Current Project with others
     */
    void startSync() {
        DivvyClient divvyClient = new DivvyClient(projectManager, user);
        if (!divvyClient.scanNetwork()) {
            log.info("No other Server Found on Network");
            return;
        }
        if (!divvyClient.connect()) {
            log.severe("Unable to connect to client");
            return;
        }
        else {
            divvyClient.sync();
        }
    }

    public ProjectManager getProjectManager() {
        return projectManager;
    }
    
    
    
}
