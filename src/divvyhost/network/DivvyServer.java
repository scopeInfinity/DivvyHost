package divvyhost.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.kryonet.rmi.ObjectSpace;
import divvyhost.configuration.Configuration;
import divvyhost.project.Details;
import divvyhost.project.Project;
import divvyhost.project.ProjectManager;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author scopeinfinity
 */
public class DivvyServer {
    private static final Logger log = Logger.getLogger(DivvyServer.class.getName());

    private ServerConnection connection;
    private Server server;
    private String user;
    
    private ProjectManager projectManager; 
    
    public DivvyServer(ProjectManager projectManager, String user) {
        this.projectManager = projectManager;
        this.user = user;
        
        server = new Server(){

            @Override
            protected Connection newConnection() {
                connection = new ServerConnection();
                return connection;
            }
            
        };
        NetworkRegister.register(server);
        
    }
    
    public boolean start() {
        try {
            server.bind(Configuration.PORT_TCP, Configuration.PORT_UCP);
            server.start();
            return true;
        } catch (IOException ex) {
            log.severe(ex.toString());
        }
        return false;
    }
    
    private class ServerConnection extends Connection implements ServerInterface {
        
        ServerConnection() {
            new ObjectSpace(this).register(NetworkRegister.RMI_SERVER, this);
        }

        @Override
        public List<Details> listDetails() {
            return projectManager.listDetails();
        }

        @Override
        public Project getProject(UUID uuid) {
            return projectManager.getProject(uuid.toString());
        }

        @Override
        public boolean isDivvyServer() {
            return true;
        }

        @Override
        public String getUser() {
            return user;
        }
        
    }
}
