package divvyhost.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryonet.rmi.ObjectSpace;
import divvyhost.project.Data;
import divvyhost.project.Details;
import divvyhost.project.Project;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author scopeinfinity
 */
public class NetworkRegister {
    
    public static int RMI_SERVER = 1;
    public static int RMI_CLIENT = 2;
    
    /**
     * Register Classes for kryo
     * @param endPoint 
     */
    static void register(EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        ObjectSpace.registerClasses(kryo);
        kryo.register(ClientInterface.class);
        kryo.register(ServerInterface.class);
        kryo.register(Details.class);
        kryo.register(Data.class);
        kryo.register(Project.class);
        
        kryo.register(ArrayList.class);
        kryo.register(List.class);
        kryo.register(PublicKey.class);
        kryo.register(UUID.class);
        kryo.register(byte[].class);
        
        
        
    }
    
}
