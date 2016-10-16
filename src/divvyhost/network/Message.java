package divvyhost.network;

import static divvyhost.network.MessageCall.SHOW_ALL_MESSAGE_LOG;
import divvyhost.project.Project;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author scopeinfinity
 */
public class Message implements Serializable {
    private static final Logger log = Logger.getLogger(Message.class.getName());
    private static final long serialVersionUID = 6008151131922371591L;
    
    public enum TYPE {Object,isDivvyServer,getUser,listDetails,getProject};
    
    private TYPE type = null;
    private Serializable value = null;
    
    Message(Serializable object) {
        this.value = object;
        type = TYPE.Object;
    }
    
    Message(TYPE procedure) {
        this.value = null;
        this.type = procedure;
    }
    
    Message(TYPE procedure, Serializable object) {
        this.value = object;
        this.type = procedure;
    }

    public TYPE getType() {
        return type;
    }

    public Serializable getValue() {
        return value;
    }
    
    /**
     * Check if Message is valid
     * @return isValid
     */
    public boolean isValid() {
        if(value!=null || this.type!=TYPE.Object)
            return true;
        return false;
    }
    
    public static class Signal implements Serializable {
        private static final long serialVersionUID = 4854081531922371591L;
        enum TYPE {Empty,Terminate};
        private TYPE type;

        public Signal(TYPE type) {
            this.type = type;
        }
        
    }
    
    /**
     * Send message via outputStream
     * @param os
     * @return isSend
     */
    public boolean send(ObjectOutputStream oos) {
        try {
            oos.writeObject(this);
            log.info("A message Send "+toString());
            return true;
        } catch (Exception ex) {
            log.severe(ex.toString());
            return false;
        }
       
    }
    
    /**
     * Receive message via inputStream
     * @param os
     * @return messageObject
     */
    public static Message receive(ObjectInputStream ois) {
        try {
            Message message = (Message) ois.readObject();
            if(message.isValid()) {
                if(SHOW_ALL_MESSAGE_LOG)
                    log.info("A message Received "+message);
                return message;
            }
        } catch (Exception ex) {
            log.severe(ex.toString());
        }
        if(SHOW_ALL_MESSAGE_LOG)
            log.info("A invalid message Received");
            
        return null;       
    }

    @Override
    public String toString() {
        return "Message{"+type+","+value+"}";
    }
    
    
    
}
