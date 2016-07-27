package divvyhost.project;

import static divvyhost.configuration.Configuration.AUTO_EXPORTPROJECT_ONLOAD;
import divvyhost.utils.Paths;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 *
 * @author scopeinfinity
 */
public class ProjectManager {
    private static final Logger log = Logger.getLogger(ProjectManager.class.getName());
 
    //pID, Project
    private HashMap<UUID, Project> availableProjects;
    
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
}
