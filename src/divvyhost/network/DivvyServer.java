package divvyhost.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.kryonet.rmi.ObjectSpace;
import divvyhost.configuration.Configuration;
import static divvyhost.configuration.Configuration.FAST_SCAN_MESSAGE;
import static divvyhost.configuration.Configuration.fastScanServerEnabled;
import divvyhost.project.Details;
import divvyhost.project.Project;
import divvyhost.project.ProjectManager;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
    
    private ServerSocket fastSocket;
    private Thread fastThread;
    private boolean isFastThreadRunning;
    
    private ProjectManager projectManager; 
    
    public DivvyServer(ProjectManager projectManager, String user) {
        this.projectManager = projectManager;
        this.user = user;
        log.info("Server Started for User :"+user);
        
        server = new Server(Configuration.BUFFER_SIZE_SERVER1, Configuration.BUFFER_SIZE_SERVER2){

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
            server.bind(Configuration.PORT_TCP);
            fastSocket = new ServerSocket(Configuration.PORT_FAST);
            server.start();
            if(fastScanServerEnabled)
                fastSockerReply();
            return true;
        } catch (IOException ex) {
            log.severe(ex.toString());
        }
        return false;
    }
    
    private void fastSockerReply() {
        isFastThreadRunning = false;
        
        fastThread = new Thread("Fast Thread"){

            @Override
            public void run() {
                isFastThreadRunning = true;
                while(isFastThreadRunning) {
                    try {
                        Socket socket = fastSocket.accept();
                        BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                        bos.write(FAST_SCAN_MESSAGE.getBytes());
                        bos.flush();
                        bos.close();
                    } catch (IOException ex) {
                        log.severe("Fast Connect Attempt Failed : "+ex.toString());
                    }
                }
            }
            
        };
        
        fastThread.start();
        
    }
    
    private class ServerConnection extends Connection implements ServerInterface {
        
        ServerConnection() {
            new ObjectSpace(this).register(NetworkRegister.RMI_SERVER, this);
        }

        @Override
        public List<Details> listDetails() {
            log.info("Client Requesting List of Projects");
            if(projectManager == null) {
                log.severe("Project Manager NULL");
                return null;
            }
            return projectManager.listDetails();
        }

        @Override
        public Project getProject(String uuid) {
            log.info("Client Requesting Project "+uuid);
            if(projectManager == null) {
                log.severe("Project Manager NULL");
                return null;
            }
            return projectManager.getProject(uuid);
        }

        @Override
        public boolean isDivvyServer() {
            log.info("Client Tested isDivvyServer()");
            return true;
        }

        @Override
        public String getUser() {
            log.info("Client taken User");
            return user;
        }
        
    }
}
