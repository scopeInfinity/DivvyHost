package divvyhost.project;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author scopeinfinity
 */
public class Details implements Serializable {
    private static final long serialVersionUID = 6008569331922371591L;
    
    private UUID pID;
    private long lastModified;

    /**
     * Fresh ProjectDetails
     */
    public Details() {
        lastModified = System.currentTimeMillis();
        pID = UUID.randomUUID();
    }

    
    public Details(UUID pID, long lastModified) {
        this.pID = pID;
        this.lastModified = lastModified;
    }

    public UUID getpID() {
        return pID;
    }

    public long getLastModified() {
        return lastModified;
    }
    
    public String getFileName() {
        return pID.toString();
    }
}
