package divvyhost.network;

import divvyhost.project.Details;
import divvyhost.project.Project;
import java.util.List;

/**
 * ServerInterface for RMI
 * @author scopeinfinity
 */
public interface ServerInterface {
    
    /**
     * Get list of Details of Projects
     * @return listDetails()
     */
    public List<Details> listDetails();
    
    /**
     * Get project from UUID
     * @param uuid
     * @return project
     */
    public Project getProject(String uuid);
    
    /**
     * For Confirmation RMI is Working and Server is valid
     * @return isDivvyServer()
     */
    public boolean isDivvyServer();
    
    /**
     * Return User ID, for maintaining connection with other userDivvy Only
     * @return 
     */
    public String getUser();
}
