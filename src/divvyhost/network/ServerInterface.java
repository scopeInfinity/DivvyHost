package divvyhost.network;

import divvyhost.project.Details;
import divvyhost.project.Project;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * ServerInterface for RMI
 * @author scopeinfinity
 */
public interface ServerInterface extends Remote{
    
    /**
     * Get list of Details of Projects
     * @return listDetails()
     */
    public List<Details> listDetails() throws RemoteException;
    
    /**
     * Get project from UUID
     * @param uuid
     * @return project
     */
    public Project getProject(String uuid) throws RemoteException;
    
    /**
     * For Confirmation RMI is Working and Server is valid
     * @return isDivvyServer()
     */
    public boolean isDivvyServer() throws RemoteException;
    
    /**
     * Return User ID, for maintaining connection with other userDivvy Only
     * @return 
     */
    public String getUser() throws RemoteException;
}
