package divvyhost.service;

import divvyhost.DivvyHost;
import divvyhost.configuration.Configuration;
import divvyhost.network.DivvyClient;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author scopeinfinity
 */
public class Server {
    private static final Logger log = Logger.getLogger(Server.class.getName());
    
    private ServerSocket socket;

    private DivvyHost divvyHost;

    public Server() {
    }
    
    /**
     * Start Service local Server
     * @return isStarted
     */
    public boolean start() {
        try {
            socket = new ServerSocket();
            socket.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), Configuration.PORT_SERVICE));
            new Thread("Service Server") {
                
                    @Override
                    public void run() {
                        while(true) {
                            try {
                                Socket client = socket.accept();
                                onClientConnect(client);
                            } catch (IOException ex) {
                                log.severe(ex.toString());
                            }
                        }
                    }
                    
            }.start();
            return true;
           
        } catch (IOException ex) {
            log.severe(ex.toString());
        }
        return false;
    }
    
    /**
     * Callback on Client Connected
     */
    private void onClientConnect(Socket client) {
        BufferedReader is = null;
        try {
            client.setSoTimeout(1000);
            is = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String message = is.readLine();
            OutputStream os = client.getOutputStream();
            String reply = getReply(message);
            log.info("Service Client Asking : " + message);
            log.info("Reply to Service Client : " + reply);
            os.write(reply.getBytes());
            os.flush();
            client.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            log.severe(ex.toString());
        } finally {
            try {
                if(is!=null)
                    is.close();
            } catch (IOException ex) {
                log.severe(ex.toString());
            }
        }
    }
    
    /**
     * Reply to Incoming Message
     * @param message
     * @return reply
     */
    private String getReply(String message) {
        if (message == null)
            return "NULL";
        if(message.equals(Service.MESSAGE_START_GUI))
            return startGUI();
        else if(message.equals(Service.MESSAGE_STOP_GUI))
            return stopGUI();
        else return "Unknown Message";
    }
    
    private String startGUI() {
        if(divvyHost == null)
            return "Divvy_NULL";
        return divvyHost.startGUI();
    }
    
    private String stopGUI() {
        if(divvyHost == null)
            return "Divvy_NULL";
        return divvyHost.stopGUI();
    }
    
    public void setDivvyHost(DivvyHost divvyHost) {
        this.divvyHost = divvyHost;
    }
    
}
