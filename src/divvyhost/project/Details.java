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

    /**
     * Equals in Sense of Project ID
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object other) {
        if(other instanceof Details)
            return pID ==  ((Details)other).getpID();
        return false;
    }
    
    /**
     * Compare is This Project is Newer
     * @param other
     * @return isNew
     */
    public boolean isNewer(Details other){
        return (lastModified < other.lastModified); 
    }
    
    
}
