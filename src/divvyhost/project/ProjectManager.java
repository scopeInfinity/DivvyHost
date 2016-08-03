package divvyhost.project;

import static divvyhost.configuration.Configuration.AUTO_EXPORTPROJECT_ONLOAD;
import divvyhost.host.Host;
import divvyhost.utils.Base64;
import divvyhost.utils.Paths;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Callback;

/**
 *
 * @author scopeinfinity
 */
public class ProjectManager {
    private static final Logger log = Logger.getLogger(ProjectManager.class.getName());
 
    private static final String BLOCK_PROJECT_FILENAME = "block_project";
    private static final String BLOCK_USER_FILENAME = "block_user";
    
//pID, Project
    private HashMap<UUID, Project> availableProjects;
    
    private Host hoster;
    
    private Set<String> blockedProjects, blockedUsers;
    
    private Paths path;

    public ProjectManager() {
        path = new Paths();
        File blockProjectFile = new File(path.getConfDir(),BLOCK_PROJECT_FILENAME );
        blockedProjects = loadUniqueRowsBlockedProject(blockProjectFile, "Project");
        File blockUserFile = new File(path.getConfDir(),BLOCK_USER_FILENAME );
        blockedUsers = loadUniqueRowsBlockedUser(blockUserFile, "User");
    }
    
    /**
     * Read Lines of String and create a HashSet, BlockedUser
     * @param file
     * @param checkValid
     * @param blockedWhat
     * @return set
     */
    private Set<String> loadUniqueRowsBlockedUser(File file, String blockedWhat ){
        HashSet<String> data = new HashSet<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = null;
            while((line = br.readLine())!=null) {
                line = line.trim();
                if(!line.isEmpty()) {
                    if( isValidUser(line) ) {
                        log.info("Blocked "+blockedWhat+" "+line);
                        data.add(line);
                    }
                }
            }
            log.info("File loaded : "+file);
            return data;
        } catch (FileNotFoundException ex) {
            log.info("No File "+file);
        } catch (IOException ex) {
            log.severe(ex.toString());
        }
        log.severe("Error in Reading");
        return data;
    }
    
    /**
     * Read Lines of String and create a HashSet, BlockedProject
     * @param file
     * @param checkValid
     * @param blockedWhat
     * @return set
     */
    private Set<String> loadUniqueRowsBlockedProject(File file, String blockedWhat ){
        HashSet<String> data = new HashSet<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = null;
            while((line = br.readLine())!=null) {
                line = line.trim();
                if(!line.isEmpty()) {
                    if( isValidProjectID(line) ) {
                        log.info("Blocked "+blockedWhat+" "+line);
                        data.add(line);
                    }
                }
            }
            log.info("File loaded : "+file);
            return data;
        } catch (FileNotFoundException ex) {
            log.info("No File "+file);
        } catch (IOException ex) {
            log.severe(ex.toString());
        }
        log.severe("Error in Reading");
        return data;
    }
    
    /**
     * Write Lines as String in File from create a HashSet
     * @param set
     * @param file
     * @return isSaved
     */
    private boolean saveUniqueRows(Set<String> set, File file){
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            for (String string : set) {
                fos.write((string+"\n").getBytes());
            }   fos.close();
            log.info("File Save : "+file);
            return true;
        } catch (FileNotFoundException ex) {
            log.severe(ex.toString());
        } catch (IOException ex) {
             log.severe(ex.toString());
        }
        return false;
    }
    
    
    /**
     * Load All Projects from Directory
     */
    public void loadAllProjects() {
        Paths path = new Paths();
        File projectDir = path.getProjectsDir();
        projectDir.mkdirs();
        log.info("Trying to load All Projects");
        availableProjects = new HashMap<>();
        for (File projectFile : projectDir.listFiles()) {
            Project project = Project.getInstance(projectFile);
            if (project == null) {
                log.severe("Invalid File Ignored : "+projectFile);
            } else {
                availableProjects.put(project.getDetails().getpID() , project);
                log.info("Project Loaded : "+projectFile);
                if (AUTO_EXPORTPROJECT_ONLOAD) {
                    if (project.autoExportProject())
                        log.info("Auto Project Exported!");
                    else 
                        log.severe("Auto Project Export Failed");
                }
            }
        }
        log.info("Done");
        
        
    }
    
    /**
     * @return  List of All Details available Projects
     */
    public List<Details> listDetails() {
        ArrayList<Details> list = new ArrayList<Details>();
        for (Map.Entry<UUID, Project> entrySet : availableProjects.entrySet()) {
            UUID key = entrySet.getKey();
            Project project = entrySet.getValue();
            list.add(project.getDetails());
        }
        return list;
    }
    /**
     * @return  List of All available Projects
     */
    public List<Project> listCompleteDetails() {
        ArrayList<Project> list = new ArrayList<Project>();
        for (Map.Entry<UUID, Project> entrySet : availableProjects.entrySet()) {
            UUID key = entrySet.getKey();
            Project project = entrySet.getValue();
            list.add(project);
        }
        return list;
    }
    
    public Project getProject(String pID) {
        if(availableProjects.containsKey(UUID.fromString(pID))) {
            return availableProjects.get(UUID.fromString(pID));
        }
        log.severe("Project Not Available : "+pID);
        return null;
    }

    /**
     * Process other Server List
     * Future : May implement Blacklisting Project
     * @param othersList
     * @return list of projects to be fetched
     */
    public List<Details> processOtherClientList(List<Details> othersList) {
        List<Details> newList = new ArrayList<Details>();
        for (Details newProject : othersList) {
            if (availableProjects.containsKey(newProject.getpID())) {
                // If Project is Newer
                if (newProject.isNewer(availableProjects.get(newProject.getpID()).getDetails()))
                    newList.add(newProject);
            } else {
                // If Project is not Present
                 newList.add(newProject);
            }
        }
        return newList;
    }

    /**
     * Set Hoster, for triggering Host Page Refresh
     * @param aThis 
     */
    public void setHoster(Host aThis) {
        this.hoster = aThis;
    }
    
    public void updateHostPage() {
        hoster.createMainPage();
    }
    
    /**
     * Check if User is Valid
     * @param uid
     * @return isValid
     */
    public static boolean isValidUser(String uid) {
        if (Base64.decode(uid).trim().isEmpty())
            return false;
        return true;
    }
    
    /**
     * Check if ProjectID is Valid
     * @param pid
     * @return isValid
     */
    public static boolean isValidProjectID(String pid) {
        try{
            UUID.fromString(pid);
            return true;
        }catch(Exception e) {
            
        }
        return false;
    }
    
    /**
     * Add Block User
     * @param user
     * @return status
     */
    public String addBlockUser(String user) {
        if(blockedUsers.contains(user))
            return "Already Blocked!";
        else if(!isValidUser(user)) {
            return "Invalid User";
        }else {
            blockedUsers.add(user);
            if(saveBlockUserList()) {
                log.info("User :"+user+ " Added to Block List");
                return "Blocked!";
            }
            log.severe("Can't Save Block List");
            return "Unable to save BlockList";
        }
    }
    
    /**
     * Add Block Project
     * @param pid
     * @return status
     */
    public String addBlockProject(String pid) {
        if(blockedProjects.contains(pid))
            return "Already Blocked!";
        else if(!isValidProjectID(pid)) {
            return "Invalid Project ID";
        }else {
            blockedProjects.add(pid);
            if(saveBlockProjectList()) {
                log.info("Project :"+pid+ " Added to Block List");
                return "Blocked!";
            }
            log.severe("Can't Save Block List");
            return "Unable to save BlockList";
        }
    }
    
    /**
     * Save blockList User To File
     * @return isSaved
     */
    private boolean saveBlockUserList() {
        File blockUserFile = new File(path.getConfDir(),BLOCK_USER_FILENAME );
        if(saveUniqueRows(blockedUsers, blockUserFile)) {
            log.info("Saved Block User List!");
            return true;
        }
        log.info("Saving Failed Block User List!");
        return false;
    }
    /**
     * Save blockList Project To File
     * @return isSaved
     */
    private boolean saveBlockProjectList() {
        File blockProjectFile = new File(path.getConfDir(),BLOCK_PROJECT_FILENAME );
        if(saveUniqueRows(blockedProjects, blockProjectFile)) {
            log.info("Saved Block Project List!");
            return true;
        }
        log.info("Saving Failed Block Project List!");
        return false;
    }
    
    

    /**
     * Can this Project be Added
     * Check if Project is New, or if Old then Check new and Valid User
     * @param newProject
     * @return 
     */
    public boolean canAddThisProject(Project newProject) {
        if(!isValidUser(newProject.getUser())) {
            log.severe("Invalid User : "+newProject.getUser());
            return false;
        }
        if (availableProjects.containsKey(newProject.getData().getpID())) {
            // Old Project
            Project oldProject = getProject(newProject.getData().getpID());
            if (!newProject.getDetails().isNewer(oldProject.getDetails())) {
                log.info("Project Not new, Fetching:"
                        +newProject.getDetails().getLastModified()
                        +" Mine:"+oldProject.getDetails().getLastModified());
                return false;
            }
            if (!newProject.getUser().equals(oldProject.getUser())) {
                log.info("Different User");
                return false;
            }
        }
        // Check if User is Blocked
        if (blockedUsers.contains(newProject.getUser()))
        {
            log.severe("Project ["+newProject.getData().getpID()+"] User ["+newProject.getUser()+"] is Blocked!");
            return false;
        }
        // Check if Project is Blocked
        if (blockedProjects.contains(newProject.getData().getpID().toString()))
        {
            log.severe("Project ["+newProject.getData().getpID()+"] is Blocked!");
            return false;
        }
        
        return true;
        
    }
    
    /**
     * Returns Project
     * @param pid
     * @return project
     */
    public Project getProject(UUID pid) {
        if(availableProjects.containsKey(pid))
            return availableProjects.get(pid);
        return null;
    }
}
