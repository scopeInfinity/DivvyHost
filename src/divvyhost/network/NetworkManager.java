package divvyhost.network;

import divvyhost.configuration.Configuration;
import divvyhost.project.ProjectManager;
import java.rmi.RemoteException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author scopeinfinity
 */
public class NetworkManager {
    private static final Logger log = Logger.getLogger(NetworkManager.class.getName());
    
    private static DivvyServer divvyServer;
    private DivvyClient divvyClient;
    private Thread serverThread;
        
    private ProjectManager projectManager; 
    private String user;
    
    private int internalScanCounter;
    
    public NetworkManager(ProjectManager projectManager, String user) {
        this.projectManager = projectManager;
        this.user = user;
        try {
            java.rmi.registry.LocateRegistry.createRegistry(Configuration.PORT_RPC);
            log.info("Created Registry :"+Configuration.PORT_RPC);
        } catch (RemoteException ex) {
            log.severe(ex.toString());
            log.severe("\nCreatingRegistry Failed!!\n\n");
        }
        internalScanCounter = 0;
        startServerThread();
    }
    
    /**
     * Start Server Thread
     */
    void startServerThread() {
        if(serverThread == null) {
            if (divvyServer!=null) {
                divvyServer.forceStop();
            }
            if(serverThread!=null)
                serverThread.stop();
            serverThread = null;
            System.gc();
            serverThread = new Thread("ServerThread"){

                @Override
                public void run() {
                    divvyServer = new DivvyServer(projectManager, user);
                    if (!divvyServer.start()) {
                        log.severe("\n\nSERVER CANNOT BE STARTED!!!!!!!!!\n\n");
                    }
                }
                
            };
            log.info("Server Thread Created!");
            serverThread.start();
            log.info("Server Thread Started!");
        } else log.info("Server Thread is Already Alive");
        log.info("Server Thread Done Check");
    }
    /**
     * Start Syncing Current Project with others
     * Currently Checking all Possible Server in One Go
     */
    void startSync() {
        internalScanCounter = 0;
        if(divvyClient==null)
            divvyClient = new DivvyClient(projectManager, user);
        
        while(true) {
            if (!divvyClient.scanNetwork()) {
                log.info("No other Server Found on Network");
                break;
            }
            
            log.info("Last Server freshOne : "+divvyClient.isLastFreshServer());
            if (!divvyClient.isLastFreshServer()) {
                divvyClient.makeServerHistoryClear();
                break;
            }
            //Boolean, null value if ignored
            Boolean status = divvyClient.connect();
            if (status!=null && !status) {
                log.severe("Unable to connect to client");
                continue;
            }
            else if(status!=null){
                divvyClient.sync();
            }
            divvyClient.disconnect();
            try {
                Thread.sleep(100);
                log.info("Sleep Server"+ ++internalScanCounter);
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
