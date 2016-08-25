package divvyhost.network;

import static divvyhost.configuration.Configuration.SCHEDULER_REFRESH_TIMER;
import divvyhost.project.Project;
import divvyhost.project.ProjectManager;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author scopeinfinity
 */
public class Scheduler implements Runnable{
    private static final Logger log = Logger.getLogger(Scheduler.class.getName());
   
    private Thread thread;
    private NetworkManager networkManager;
    
    private ProjectManager projectManager;
    private String user;
    
    private boolean syncInProcess;
    
    public Scheduler(ProjectManager manager, String user) {
        this.user = user;
        this.projectManager = manager;
        thread = new Thread(this,"Scheduler");
        thread.setPriority(Thread.MIN_PRIORITY);
    }
    
    private void periodicEvent() {
        //Only one Sync at a time
        if(syncInProcess)
            return;
        syncInProcess = true;
        
        log.info("Scheduler Perdiodic Start");
        try{
            log.info("Going for Sync");
            networkManager.startSync();
        }catch(Exception e) {
            log.severe("Scheduler Sync Error " + e.toString());
        }
        syncInProcess = false;
    }
    
    public void start() {
        networkManager = new NetworkManager(projectManager,user);
        thread.start();
        syncInProcess = false;
    }
    
    @Override
    public void run() {
        try {
            while(true)
            {     
                periodicEvent();
                thread.sleep(SCHEDULER_REFRESH_TIMER);
            }
        } catch (InterruptedException ex) {
           log.severe(ex.toString());
           log.severe("SCHEDULER TERMINATED!!!!!");
        }
    }
    
}
