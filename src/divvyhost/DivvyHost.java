package divvyhost;

import divvyhost.GUI.Controller;
import divvyhost.GUI.Main;
import divvyhost.network.Scheduler;
import divvyhost.project.Data;
import divvyhost.project.Details;
import divvyhost.project.Project;
import divvyhost.project.ProjectManager;
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

    private static User user;
    private Main gui;
    private Controller controller;
    private ProjectManager projectManager;
    private Scheduler scheduler;
    
    private boolean guiLoading;
    
    private boolean needGUI;

    public DivvyHost() {
        user = User.loadUser();
        guiLoading = true;
        controller = new Controller(this);
        projectManager = new ProjectManager();
        scheduler = new Scheduler(projectManager, user.getUser());
        log.info("Divvy Host Created!");
    }
    
    public void start() {
        projectManager.loadAllProjects();
        scheduler.start();
        if (needGUI) {
            log.info("Waiting For GUI Loading...");
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    Main main = new Main(controller);
                    main.setVisible(true);
                    main.setTitle("Divvy Host");
                    setUIWaitOver();
                }
            });
            while(guiLoading);
            log.info("GUI Loading Done!");
        } else
            log.info("GUI Disabled");
        
    }
    
    public void setUIWaitOver() {
        guiLoading = false;
        log.info("UI Loading Complete");
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DivvyHost divvy = new DivvyHost();
        divvy.checkParameters(Arrays.asList(args));
        divvy.start();
        
    }
    
    /**
     * Use command-line parameters
     * @param param 
     */
    private void checkParameters(List<String> param){
        if (param.contains("-nogui"))
            needGUI = false;
        else 
            needGUI = true;
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
       
 }
