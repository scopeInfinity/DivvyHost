package divvyhost.GUI;

import divvyhost.DivvyHost;
import divvyhost.project.Data;
import divvyhost.project.Details;
import divvyhost.project.Project;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 *
 * @author scopeinfinity
 */
public class Controller {
    private static final Logger log = Logger.getLogger(Controller.class.getName());

    DivvyHost host;
    public Controller(DivvyHost divvy) {
        host = divvy;
    }
    
    /**
     * List of all available Projects
     * @return listOfProjects
     */
    public List<Project> getProjectList() {
        host.getProjectManager().loadAllProjects();
        return host.getProjectManager().listCompleteDetails();
    }
    
    /**
     * Create new Project HostingDirectory+Saved File
     * @param author
     * @param title
     * @param description
     * @return Status
     */
    public String createNewProject(String author, String title, String description) {
        Details details = new Details();
        Data data = new Data(details.getpID(), author, title, description);
        Project project = new Project(host.getUser().getPublicKey(), details, data);
        project.createHostPath();
        if (project.importProject(null, null, null, host.getUser().getPrivatekey()))
            if(project.save()) {
                log.info("New Project Created Successfully!");
                return "New Project ID "+details.getpID();
            }
        log.info("Project Creation Failed!");
        return "Project Creation Failed!";
    }
    
    /**
     * Pull Project using pID
     * @param pID
     * @return status
     */
    public String pullProject(String pID) {
        try{
            UUID.fromString(pID);
        }catch(Exception e) {
            return "Invalid Project ID";
        }
        Project project = host.getProjectManager().getProject(pID);
        if (project!=null) {
               if(!project.importProject(null, null, null, host.getUser().getPrivatekey())) 
                   return "Import Failed [Possiblity - Unauthorised User or SomeOther Reason]";
               else
               {
                   if (project.save()) {
                       host.getProjectManager().updateHostPage();
                       return "Pulled Successfull";
                   } else {
                       return "Error in Saving After Pulling";
                   }
               }
        } else {
            return "Project not Found!";
        }
    }
    
    /**
     * Export Project from pID
     * @param pID
     * @return status
     */
    public String exportProject(String pID) {
        try{
            UUID.fromString(pID);
        }catch(Exception e) {
            return "Invalid Project ID";
        }
        Project project = host.getProjectManager().getProject(pID);
        if (project!=null) {
               if(!project.exportProject()) 
                   return "Export Failed";
               else {
                   host.getProjectManager().updateHostPage();
                   return "Exported Successfully!";
               }
        } else {
            return "Project not Found!";
        }
    }
    
    /**
     * Blocks a User
     * @param user
     * @return Status
     */
    public String blockUser(String user) {
        return host.getProjectManager().addBlockUser(user);
    }
    
    /**
     * Block a Project
     * @param pID
     * @return Status
     */
    public String blockProject(String pID) {
        return host.getProjectManager().addBlockProject(pID);
    }
}
