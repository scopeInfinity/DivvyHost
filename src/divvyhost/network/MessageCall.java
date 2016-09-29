package divvyhost.network;

import divvyhost.project.Details;
import divvyhost.project.Project;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author scopeinfinity
 */
public class MessageCall implements ServerInterface {
    private static final Logger log = Logger.getLogger(MessageCall.class.getName());
    public static final boolean SHOW_ALL_MESSAGE_LOG = false;
    
    private Socket socket;
    
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    

    public MessageCall(Socket socket) throws IOException {
        this.socket = socket;
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
    }
    
    public void close() {
        try {
            socket.close();
        } catch (IOException ex) {
            log.severe(ex.toString());
        }
    }
    
    /**
     * Send Message and return reply
     * @param message
     * @return 
     */
    private Message process(Message message) {
        Message reply = null; 
        try{
            if(message.send(outputStream)) {
                if(SHOW_ALL_MESSAGE_LOG)
                    log.info("Message send to Server");
                reply = Message.receive(inputStream);
                if(reply == null) {
                    if(socket.isConnected()) {
                        log.info("Closing Connection from Client");
                        socket.close();
                    }
                }
            }
        }catch(Exception e) {
            log.severe("Process Failed "+e);
            try {
                socket.close();
            } catch (IOException ex) {
            log.severe(e.toString());
            }
        }
        return reply;
    }

    @Override
    public List<Details> listDetails() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Project getProject(String uuid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isDivvyServer() throws MessageFailure {
        Message r = process(new Message(Message.TYPE.isDivvyServer));
        if(r== null)
            throw new MessageFailure("Message Received is NULL");
        if(r.getType() == Message.TYPE.Object)
            return (Boolean) r.getValue();
        throw new MessageFailure(Message.TYPE.Object,r.getType());
    }

    @Override
    public String getUser() throws MessageFailure{
        Message r = process(new Message(Message.TYPE.getUser));
        if(r== null)
            throw new MessageFailure("Message Received is NULL");
        if(r.getType() == Message.TYPE.Object)
            return (String) r.getValue();
        throw new MessageFailure();
    }
      
    static class MessageFailure extends Exception {

        public MessageFailure() {
            super("Error in Processing");
        }
        
        public MessageFailure(String s) {
            super("Error in Processing, "+s);
        }
        
        public MessageFailure(Message.TYPE need, Message.TYPE found) {
            super("Error in Processing, need "+need+" found "+found);
        }
        
    }
}
