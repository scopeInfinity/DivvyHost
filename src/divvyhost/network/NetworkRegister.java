package divvyhost.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryonet.rmi.ObjectSpace;
import divvyhost.project.Data;
import divvyhost.project.Details;
import divvyhost.project.Project;
import divvyhost.project.ProjectManager;
import divvyhost.utils.Pair;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/**
 *
 * @author scopeinfinity
 */
public class NetworkRegister {
    private static final Logger log = Logger.getLogger(NetworkRegister.class.getName());
 
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
        kryo.register(Project.class);
        kryo.register(Data.class);
        kryo.register(Details.class);
        
        kryo.register(ArrayList.class);
        kryo.register(List.class);
        kryo.register(byte[].class);
        
        kryo.register(PublicKey.class);
        
        // UUID is not Serializable
        // Thus no Member containing UUID should not be passed using kryonet
        kryo.register(UUID.class);
        
        
    }
    
    
    /**
     * For Recursively Registering Classes
     * Maintain Collection of classes already Registered
     */
    private static HashMap<EndPoint, Set<Class> > alreadyRegistered;
    
    /**
     * Register Class to given EndPoint recursively
     * @param endPoint
     * @param _class 
     */
    private static void registerClass(EndPoint endPoint, Kryo kryo, Class _class) {
        Set<Class> registered;
        if (alreadyRegistered == null) {
            alreadyRegistered = new HashMap<>();
        }
        if (alreadyRegistered.containsKey(endPoint))
            registered = alreadyRegistered.get(endPoint);
        else {
            registered = new HashSet<>();
            alreadyRegistered.put(endPoint, registered);
        }
        
        Field[] fields = _class.getDeclaredFields();
        ArrayList<ClassPair> list = new ArrayList<>();
        if (!registered.contains(_class)) {
            list.add(new ClassPair(_class.getCanonicalName(), _class));
            registered.add(_class);
        }
        
        for (Field field : fields) {
            String name = field.getName();
            Class classMember = field.getType();
            if(!classMember.isPrimitive()) {
                //Need to Register
                if (!registered.contains(classMember)) {
                    list.add(new ClassPair(classMember.getCanonicalName(), classMember));
                    registered.add(classMember);
                }
            } 
            
        }
        
        //Sorting in Order of ClassName locally in recurstion
        list.sort(null);
        
        //Registering Classes in Sorted Order
        for (ClassPair classPair : list) {
            kryo.register(classPair.getSecond());
            log.info("Register Class : "+classPair.getFirst());
        }
        
        //Recursive goint in bottom
        for (ClassPair classPair : list) {
            kryo.register(classPair.getSecond());
        }
        
    }
    
    private static class ClassPair extends Pair<String,Class> implements Comparable<ClassPair>{

        public ClassPair(String first, Class second) {
            super(first, second);
        }

        @Override
        public int compareTo(ClassPair other) {
            return getFirst().compareTo(other.getFirst());
        }
    }
    
}

