package divvyhost.network;

import divvyhost.configuration.Configuration;
import static divvyhost.configuration.Configuration.FAST_SCAN_MESSAGE;
import static divvyhost.configuration.Configuration.fastScanServerEnabled;
import divvyhost.project.Details;
import divvyhost.project.Project;
import divvyhost.project.ProjectManager;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
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

    private ServerConnection server;
    private String user;
    
    private static ServerSocket fastSocket;
    private Thread fastThread;
    private boolean isFastThreadRunning;
    
    private ProjectManager projectManager; 
    
    public DivvyServer(ProjectManager projectManager, String user) {
        this.projectManager = projectManager;
        this.user = user;
        log.info("Server Started for User :"+user);
        
        server = null;
                
    }
    
    public boolean start() {
        try {
            server = new ServerConnection();
            Naming.rebind("rmi://127.0.0.1/divvy", server);
            rebindFastSocket();
            if(fastScanServerEnabled)
                fastSockerReply();
            return true;
        } catch (IOException ex) {
            log.severe(ex.toString());
        }
        return false;
    }
    
    private void rebindFastSocket() {
        try{
            if(fastSocket==null)
                fastSocket = new ServerSocket(Configuration.PORT_FAST);
            log.info("Fast Socket Binded");
        }catch(BindException e) {
            log.info("Fast Socket Already Binded");
        } catch (IOException ex) {
          log.info("Fast Socket Already Binded");
        }
    }
    
    private void fastSockerReply() {
        isFastThreadRunning = false;
        
        fastThread = new Thread("Fast Thread"){

            @Override
            public void run() {
                if(fastScanServerEnabled) {
                    if(fastSocket == null) {
                        rebindFastSocket();
                    }
                    if(fastSocket == null) {
                        log.severe("FastSocket is NULL, can't Reply!!! ");
                    }
                }
                isFastThreadRunning = true;
                while(isFastThreadRunning) {
                    try {
                        Socket socket = fastSocket.accept();
                        System.out.println("FASTSOCKET : "+fastSocket);
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
    
    /**
     * Force Stop Server
     */
    @Deprecated
    public void forceStop() {
        log.severe("Calling @Deprecated, forceStop");
                
        isFastThreadRunning = false;
        if(server!=null) {
            server = null;
            log.info(">>>>> Foce Stopping");
        }
        if(fastThread!=null)
            fastThread.stop();
    }
    
    private class ServerConnection extends UnicastRemoteObject implements ServerInterface {
        
        ServerConnection() throws RemoteException {
            
        }

        @Override
        public List<Details> listDetails() throws RemoteException {
            log.info("Client Requesting List of Projects");
            if(projectManager == null) {
                log.severe("Project Manager NULL");
                return null;
            }
            return projectManager.listDetails();
        }

        @Override
        public Project getProject(String uuid) throws RemoteException  {
            log.info("Client Requesting Project "+uuid);
            if(projectManager == null) {
                log.severe("Project Manager NULL");
                return null;
            }
            return projectManager.getProject(uuid);
        }

        @Override
        public boolean isDivvyServer()  throws RemoteException {
            log.info("Client Tested isDivvyServer()");
            return true;
        }

        @Override
        public String getUser() throws RemoteException  {
            log.info("Client taken User");
            return user;
        }

    }
}
