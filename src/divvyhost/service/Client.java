package divvyhost.service;

import divvyhost.configuration.Configuration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author scopeinfinity
 */
public class Client {
    private static final Logger log = Logger.getLogger(Client.class.getName());
    
    private Socket socket;
    private boolean isRunning;
    
    public Client() {
        
    }
    
    public boolean connect() {
        try {
            socket = new Socket("127.0.0.1", Configuration.PORT_SERVICE);
            return true;
        } catch (IOException ex) {
            log.severe(ex.toString());
        }
        return false;
    }

    /**
     * Send Message to Service Server
     * @param message 
     */
    private String sendMessage(String message) {
        if(socket == null) {
            log.severe("No Service Socket");
            return null;
        }
        if(!socket.isConnected()) {
            log.severe("Service Socket Not Connected");
            return null;
        }
        
        BufferedReader is = null;
        try {
            OutputStream os = socket.getOutputStream();
            os.write((message+"\n").getBytes());
            log.info("Service Message Send : "+message);
            os.flush();
            is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String reply = is.readLine();
            log.info("Service Message Reply : "+reply);
            socket.close();
            socket = null;
            return reply;
        } catch (Exception ex) {
            log.severe(ex.toString());
        } finally {
            try {
                if(is!=null)
                    is.close();
            } catch (IOException ex) {
                log.severe(ex.toString());
            }
        }
        log.severe("No Reply Message");
        return null;
                
    }
    
    public void startGUI() {
        sendMessage(Service.MESSAGE_START_GUI);
    }
    
    public void stopGUI() {
        sendMessage(Service.MESSAGE_STOP_GUI);
    }
    
}
