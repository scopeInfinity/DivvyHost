package divvyhost.network;

import divvyhost.configuration.Configuration;
import static divvyhost.configuration.Configuration.FAST_SCAN_MESSAGE;
import static divvyhost.configuration.Configuration.fastScanServerEnabled;
import static divvyhost.network.MessageCall.SHOW_ALL_MESSAGE_LOG;
import divvyhost.project.Details;
import divvyhost.project.Project;
import divvyhost.project.ProjectManager;
import divvyhost.utils.Paths;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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
    private Thread serverThread;
    private boolean isFastThreadRunning;
    private boolean isServerThreadRunning;
    
    private ProjectManager projectManager; 
    
    public DivvyServer(ProjectManager projectManager, String user) {
        this.projectManager = projectManager;
        this.user = user;
        log.info("Server Started for User :"+user);
        
        server = null;
                
    }
    
    public boolean start() {
        checkAndRunServerSocket();
        rebindFastSocket();
        if(fastScanServerEnabled)
            fastSockerReply();
        return true;
    }
    
    /**
     * Check if server not running
     */
    private void checkAndRunServerSocket() {
        serverThread = new Thread("ServerSocket Thread"){
            
            @Override            
            public void run() {
                isServerThreadRunning = true;
                while(isServerThreadRunning) {
                    try {
                        if(server==null || !server.isRunning())
                            server = new ServerConnection();
                        sleep(Configuration.SERVER_REFRESH_TIMER);
                    } catch (Exception e) {
                        log.severe("Server Socker Failed!\n\n");
                        e.printStackTrace();
                    }
                }
            }
                        
            
        };
        serverThread.setPriority(Thread.MIN_PRIORITY);
        serverThread.start();
        
        
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
        isServerThreadRunning = false;
        if(serverThread!=null) {
            serverThread.stop();
            server = null;
            log.info(">>>>> Foce Stopping");
        }
        if(fastThread!=null)
            fastThread.stop();
    }
    
    private class ServerConnection implements ServerInterface {
        private Socket clienthandle;
        private ObjectInputStream is;
        private ObjectOutputStream os;
        
        ServerConnection() throws Exception {
            dotask();
        }
        
        private void close() {
            if(clienthandle != null && clienthandle.isConnected())
                try {
                    clienthandle.close();
            } catch (IOException ex) {
            }
            clienthandle = null;
            is=null;
            os=null;
            log.info("Closing Connection");
        }
        
        private void dotask() throws Exception {
            while(true) {
                ServerSocket socket = new ServerSocket(Configuration.PORT_RPC);
                clienthandle = socket.accept();
                is = new ObjectInputStream(clienthandle.getInputStream());
                os = new ObjectOutputStream(clienthandle.getOutputStream());
                
                while(true) {
                    if(!clienthandle.isConnected())
                        break;
                    Message message = Message.receive(is);
                    if(SHOW_ALL_MESSAGE_LOG)
                        log.info("Server Received a Message");
                    if(!reply(message,os))
                    {
                        socket.close();
                        break;
                    }
                }
            }
            
        }
        
        /**
         * Reply for a given Message
         * @param message
         * @param os 
         * @return hasReply and notTerminated
         */
        private boolean reply(Message message, ObjectOutputStream os) {
            Message replyblock;
            if (message == null) {
                close();
                return false;
            } else if(message.getType() == Message.TYPE.isDivvyServer) {
                replyblock = new Message(Boolean.valueOf(isDivvyServer()));
            } else if(message.getType() == Message.TYPE.getUser) {
                replyblock = new Message(getUser());
            } else {
                close();
                return false;
            }
            if(!replyblock.send(os)) {
                log.severe("Message not send! Closing Connection");
                close();
                return false;
            }
            return true;
        }
        
        private boolean isRunning() {
            if(clienthandle==null || !clienthandle.isConnected())
                return false;
            return true;
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
        public Project getProject(String uuid)  {
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
        public String getUser()  {
            log.info("Client taken User");
            return user;
        }

        

        

    }
}
