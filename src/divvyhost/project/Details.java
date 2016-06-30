package divvyhost.project;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author scopeinfinity
 */
public class Details implements Serializable {
    private UUID pId, uID;
    private long lastModified;
    private byte[] checkSum;

    public Details(UUID pId, UUID uID, long lastModified, byte[] checkSum) {
        this.pId = pId;
        this.uID = uID;
        this.lastModified = lastModified;
        this.checkSum = checkSum;
    }

    public byte[] getCheckSum() {
        return checkSum;
    }

    public UUID getpId() {
        return pId;
    }

    public UUID getuID() {
        return uID;
    }

    public long getLastModified() {
        return lastModified;
    }
    
    public String getFileName() {
        return uID.toString();
    }
}
