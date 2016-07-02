package divvyhost.project;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author scopeinfinity
 */
public class Details implements Serializable {
    private UUID pID, uID;
    private long lastModified;

    /**
     * Fresh ProjectDetails
     */
    public Details(UUID user) {
        lastModified = System.currentTimeMillis();
        pID = UUID.randomUUID();
        uID = user;
    }

    
    public Details(UUID user, UUID pID, long lastModified) {
        this.pID = pID;
        this.uID = user;
        this.lastModified = lastModified;
    }

    public UUID getpID() {
        return pID;
    }

    public UUID getuID() {
        return uID;
    }

    public long getLastModified() {
        return lastModified;
    }
    
    public String getFileName() {
        return pID.toString();
    }
}
