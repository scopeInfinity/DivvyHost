package divvyhost;

import divvyhost.GUI.Controller;
import divvyhost.GUI.Main;
import divvyhost.configuration.Configuration;
import divvyhost.host.Host;
import divvyhost.network.Scheduler;
import divvyhost.project.Data;
import divvyhost.project.Details;
import divvyhost.project.Project;
import divvyhost.project.ProjectManager;
import divvyhost.service.Service;
import divvyhost.users.User;
import divvyhost.utils.Base64;
import divvyhost.utils.Pair;
import divvyhost.utils.Paths;
import divvyhost.utils.Utils;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 *
 * @author scopeinfinity
 */
public class DivvyHost {
    private static final Logger log = Logger.getLogger(DivvyHost.class.getName());

    private static Service service;
    private static User user;
    
    private Controller controller;
    private ProjectManager projectManager;
    private Scheduler scheduler;
    private Host hoster;
    
    private boolean needGUI;
    private Main mainGUI;

    public DivvyHost() {
        user = User.loadUser();
        controller = new Controller(this);
        projectManager = new ProjectManager();
        scheduler = new Scheduler(projectManager, user.getUser());
        hoster = new Host(projectManager);
        service.setDivvyHost(this);
        log.info("Divvy Host Created!");
    }
    
    public boolean start() {
        Configuration configuration = new Configuration();
        if (!configuration.isLoadedFine()) {
            log.severe("Unable to load Configurations");
            return false;
        }
        projectManager.loadAllProjects();
        hoster.createMainPage();
        scheduler.start();
        if (needGUI) {
            log.info("Tring For GUI Creation...");
            createGUI();
        } else
            log.info("GUI Disabled");
        
        hoster.start();
        return true;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        service = new Service();
        
        DivvyHost divvy = new DivvyHost();
        if(!divvy.checkParameters(Arrays.asList(args)))
            return;
        
        if(!divvy.start())
            log.severe("Divvy Start Failed!");
        
    }
    
    /**
     * Use command-line parameters
     * @param param 
     * @return needToContinue
     */
    private boolean checkParameters(List<String> params){
        String serviceFlag = null;
        for (String param : params) {
            if(param.startsWith("-service="))
                serviceFlag = param.substring("-service=".length());
        }
        if(!service.start(serviceFlag)) { 
            log.info("Quitting");
            return false;
        }
            
        if (params.contains("-nogui"))
            needGUI = false;
        else 
            needGUI = true;
        return true;
    }
    
    /**
     * Create Project and Load Project Test Case
     * @param pID
     * @return 
     */
    static String simpleTestsCreate(String pID) {
        
        //Init
        Details detail;
        if(pID != null)
            detail = new Details(UUID.fromString(pID), System.currentTimeMillis());
        else
        {
            detail = new Details();
            pID = detail.getpID().toString();
        }
        
        Data data = new Data(UUID.fromString(pID), "Gagan", "Test", "Description");
        Project project = new Project(user.getPublicKey(), detail, data);
        project.createHostPath();
        
        //Import Project
        System.out.println("Imported : "+project.importProject(null, null, null, user.getPrivatekey()));
        
        //Saving
        System.out.println("Saved : "+project.save());
        return detail.getpID().toString();
        
    }
    
    /**
     * Open and Export Project Test Case
     * @param pID 
     */
    static void simpleTestExport(String pID) {
        
        //Trying to Load
        Project oldProject = Project.load(pID);
        if(oldProject == null) {
            System.err.println("Project Load Failed!");
            return;
        }
        System.out.println("User : "+oldProject.getUser());
        System.out.println("PID : "+oldProject.getDetails().getpID());
        System.out.println("LastModified : "+oldProject.getDetails().getLastModified());
        System.out.println("Title : "+oldProject.getData().getTitle());
        System.out.println("Description : "+oldProject.getData().getDescription());
        System.out.println("Signature : "+Base64.encode(oldProject.getSignature()));
        System.out.println("PublicKey : "+Base64.encode(oldProject.getPublicKey().getEncoded()));
        System.out.println("Data Null : "+(oldProject.getData().getData()==null));
        
        if(oldProject.getData().getData()!=null)
            System.out.println("Data length : "+oldProject.getData().getData().length);
        
        System.out.println("\nExported : "+oldProject.exportProject());
        
    }

    public ProjectManager getProjectManager() {
        return projectManager;
    }

    public static User getUser() {
        return user;
    }
       
    
    private void createGUI() {
        if(mainGUI!=null) {
            log.severe("GUI Already There");
            return;
        }
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                mainGUI = new Main(controller);
                mainGUI.setVisible(true);
                mainGUI.setTitle("Divvy Host");
                log.info("GUI Loading Done");
            }
        });
        
    }
    /**
     * Callback from Service
     * @return status
     */
    public String startGUI() {
        if(mainGUI!=null)
            return "GUI Already Exists";
        else
        {
            createGUI();
            return "GUI Start Called";
        }
                    
    }
    
    /**
     * Callback from Service
     * @return status
     */
    public String stopGUI() {
        if(mainGUI == null)
            return "GUI No Present";
        else
        {
            mainGUI.setVisible(false);
            mainGUI = null;
            System.gc();
            return "GUI Stop Called";
        }
    }
    
 }
