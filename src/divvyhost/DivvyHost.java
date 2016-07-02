package divvyhost;

import divvyhost.project.Data;
import divvyhost.project.Details;
import divvyhost.project.Project;
import divvyhost.utils.Pair;
import divvyhost.utils.Paths;
import divvyhost.utils.Utils;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.UUID;
import javax.xml.soap.Detail;

/**
 *
 * @author scopeinfinity
 */
public class DivvyHost {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String pID = "1e0cf5a7-4884-463f-94c2-cf5e95a3f976";
        //simpleTestsInit(pID);
        simpleTestLoad(pID);
    }
    
    static String simpleTestsInit(String pID) {
        
        //Init
        Paths path = new Paths();
        Pair<PublicKey, PrivateKey> keys = Utils.generateDSAKeys();
        UUID user = UUID.fromString("28785d34-3f05-45d8-93ab-eee2211f1100");
        System.out.println("User ID :"+user.toString());
        Details detail;
        if(pID != null)
            detail = new Details(user, UUID.fromString(pID), System.currentTimeMillis());
        else
            detail = new Details(user);
        Data data = new Data("Test", "Description");
        Project project = new Project(keys.getFirst(), detail, data);
        project.createHostPath();
        
        //Import Project
        System.out.println("Imported : "+project.importProject(null, null, keys.getSecond()));
        
        //Saving
        System.out.println("Saved : "+project.save());
        return detail.getpID().toString();
        
    }
    
    static void simpleTestLoad(String pID) {
        
        //Trying to Load
        Project oldProject = Project.load(pID);
        if(oldProject == null) {
            System.err.println("Project Load Failed!");
            return;
        }
        System.out.println("UID : "+oldProject.getDetails().getuID());
        System.out.println("PID : "+oldProject.getDetails().getpID());
        System.out.println("LastModified : "+oldProject.getDetails().getLastModified());
        System.out.println("Title : "+oldProject.getData().getTitle());
        System.out.println("Description : "+oldProject.getData().getDescription());
        System.out.println("Signature : "+Base64.getEncoder().encodeToString(oldProject.getSignature()));
        System.out.println("PublicKey : "+Base64.getEncoder().encodeToString(oldProject.getPublicKey().getEncoded()));
        System.out.println("Data Null : "+(oldProject.getData().getData()==null));
        
        if(oldProject.getData().getData()!=null)
            System.out.println("Data length : "+oldProject.getData().getData().length);
        
        System.out.println("\nExported : "+oldProject.exportProject());
        
    }
 }
