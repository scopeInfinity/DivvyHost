package divvyhost.network;

import divvyhost.configuration.Configuration;
import static divvyhost.configuration.Configuration.FAST_SCAN_MESSAGE;
import static divvyhost.configuration.Configuration.fastScanEnabled;
import divvyhost.project.Details;
import divvyhost.project.Project;
import divvyhost.project.ProjectManager;
import divvyhost.utils.Utils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author scopeinfinity
 */
public class DivvyClient implements ClientInterface{
    private static final Logger log = Logger.getLogger(DivvyClient.class.getName());
    
    private ProjectManager projectManager; 
    private Configuration configuration;
    private String user;
    
    private MessageCall divvyServer;
    private Socket divvyServerSocket;
    
    //Set Containing IP Address of any server encountered
    private Set<InetAddress> serverFound;
    
    //IP Address of Last Successfull Server
    private InetAddress lastScannedAddress;
    //Index of IP from configuration file,which is last scanned
    private int lastScannedIPListIndex;
    //Suffix of last IP, which is last scanned
    private int lastScannedIPSuffix;
            
    public DivvyClient(ProjectManager projectManager, String user) {
        this.projectManager = projectManager;
        this.user = user;
        configuration = new Configuration();
    
        //Not in reset, may be used in some optimization later
        serverFound = new HashSet<InetAddress>();
        
        reset();            
    }
    
    /**
     * Reset Object for reuse
     */
    public void reset() {
        lastScannedIPListIndex = 0;
        lastScannedIPSuffix = -1;
        lastScannedAddress = null;
        disconnect();
    }
     
    /**
     * Get Remote Object from RMI
     * @ address
     * @return gotRemoteObject.
     */
    private boolean getRemoteObj(InetAddress address) {
        try {
            //        try {
////            if (System.getSecurityManager() != null) {
////                System.setSecurityManager(null);
////            }
////            Registry registry = LocateRegistry.getRegistry(lastScannedAddress.getHostAddress().toString(), Configuration.PORT_RPC);
////            
////            divvyServer = (ServerInterface) registry.lookup("divvy");
//
////            System.getProperties().put("java.rmi.server.hostname", ");
////            
////            divvyServer = (ServerInterface) Naming.lookup(
////                    String.format("rmi://%s:%s/",
////                            lastScannedAddress.getHostAddress().toString(),
////                            String.valueOf(Configuration.PORT_RPC)) 
////                            + "divvy");
//            log.info("Got Server Object : "+lastScannedAddress.getHostAddress());
//            log.info("Test : "+divvyServer.isDivvyServer()+"\n\n\n");
//            log.info(""+divvyServer.getUser()+"\n\n\n");
//
//            return true; 
//        } catch (NotBoundException | RemoteException ex) {
//            log.severe(ex.toString());
//            ex.printStackTrace();
////        } catch (MalformedURLException ex) {
////            log.severe(ex.toString());
//        }
//      
            divvyServerSocket = new Socket(address, Configuration.PORT_RPC);
            divvyServer = new MessageCall(divvyServerSocket);
            return true;
        } catch (Exception ex) {
            log.severe("Server Object get Failed! : "+lastScannedAddress.getHostAddress());
        }
        log.severe("Got Server Object : "+lastScannedAddress.getHostAddress());
        return false;
    }
    
    /**
     * Scan server if available on current network
     * @return isFound.
     */
    public boolean scanNetwork() {
        InetAddress lastScanIP = lastScannedAddress;
        lastScannedAddress = null;
        
        //Using Configuration File
        if (checkServerOnSubnet(configuration.getInternalIPs(), configuration.getPrefixLengths(), lastScanIP)) {
            log.info("Server Found");
        }           
        
        if(lastScannedAddress == null) {
            log.info("No New Server Found");
            return false;
        }
        
        log.info("Server["+lastScannedAddress.getHostAddress()+"] Found");
        return true;
    }
    
    /**
     * Connect if last find Server, Returns null if Server is mine
     * @return isConnected
     */
    public Boolean connect() {
        if(lastScannedAddress == null) {
            log.severe("No Scanned Host");
            return false;
        }
        try {
                if(!getRemoteObj(lastScannedAddress))
                {
                   return false;
                }
                //Verifing Other Server
                try{
                    if(divvyServer.isDivvyServer()) {
                        String otherUser = divvyServer.getUser();
                        log.info("Server["+lastScannedAddress+"] ServerUser ("+otherUser + "), Me ("+user+")");
                        if (otherUser!=null && !otherUser.equals(user)) {
                            return true;
                        } else {
                            log.info("Ignored due to Server User");
                            return null;
                        }
                    }
                }catch(Exception e) {
                    log.severe("Invalid Server : "+e);
                }

                return false;
        } catch (Exception ex) {
                log.severe(ex.toString());
        } finally {
           
        }
        return false;
    }
    
    /**
     * Disconnect connection with server if connected
     */
    public void disconnect() {
        lastScannedAddress = null;
        if(divvyServerSocket!=null && divvyServerSocket.isConnected())
            try {
                divvyServerSocket.close();
            } catch (IOException ex) {
            }
        divvyServerSocket = null;
        divvyServer = null;
    }

    /**
     * Request Server for project Update
     */
    public void sync() {
        if (divvyServer == null) {
            log.severe("SYNC failed, proxy interface is null");
            return;
        }
        
        List<Details> othersList = null;
        try {
            othersList = divvyServer.listDetails();
        } catch (Exception ex) {
            log.info("Failed to Fetch Others List");
            disconnect();
            return;
        }
        
        
        log.info("Number of Project of Server["+lastScannedAddress.getHostAddress()+"] "+othersList.size());
        
        try {
            List<Details> fetchThese = projectManager.processOtherClientList(othersList);
            log.info("Number of New Project of Server["+lastScannedAddress.getHostAddress()+"] "+fetchThese.size());

            for (Details projectDetails : fetchThese) {

                Project newProject = null;
                try {
                    newProject = divvyServer.getProject(projectDetails.getpID().toString());
                } catch (Exception ex) {
                    log.severe("Server["+lastScannedAddress.getHostAddress()+"] Project Failed to Fetch :"+projectDetails.getpID() );
                    continue;
                }
                if (!projectManager.canAddThisProject(newProject)) {
                    log.severe("Server["+lastScannedAddress.getHostAddress()+"] Project, Can't Add this Project :"+projectDetails.getpID() );
                    continue;
                }
                if (!newProject.completeValidation()) {
                    log.severe("Server["+lastScannedAddress.getHostAddress()+"] Project Validation Failed :"+projectDetails.getpID() );
                    continue;
                }
                if (!newProject.save()) {
                    log.severe("Server["+lastScannedAddress.getHostAddress()+"] Project Failed to Save : "+newProject.getDetails().getFileName());
                    continue;
                }
                if (!newProject.exportProject()) {
                    log.severe("Server["+lastScannedAddress.getHostAddress()+"] Project Failed to Export : "+newProject.getDetails().getFileName());
                    continue;
                }
            }
        } catch (Exception ex) {
            log.info("Failed to Fetch Others List");
            disconnect();
            return;
        }
        
    }
    
    /**
     * Find any Server Running on given IP/Subnet
     * Note : On IPv4 Only
     * @param address
     * @param prefixLength
     * @return isServerFound
     */
    private boolean checkServerOnSubnet(List<InetAddress> inetAddresses, List<Integer> prefixLengths, InetAddress lastScanIP) {
        if (inetAddresses.size() == 0) {
            log.info("No Address Provided for Scan! Returing");
            return false;
        }
        
        try{
            for(int i=lastScannedIPListIndex;i<inetAddresses.size();i++) {
                if(lastScannedIPListIndex!=i)
                    lastScannedIPSuffix = -1;
                lastScannedIPListIndex = i;
                    
                int scanIndex = i;

                InetAddress inetAddress = inetAddresses.get(scanIndex);
                int prefixLength = prefixLengths.get(scanIndex);

                if (inetAddress.getAddress().length!=4) {
                    // Only for IPv4
                    continue;
                }

                //IPv4
                byte[] address = inetAddress.getAddress();
                int suffixMask0 = 32 - prefixLength;

                //Masking The Address
                for (int j = 0; j < 4; j++) {
                    int mySuffix = suffixMask0 - 8*(3-j);
                    mySuffix = Math.min(mySuffix, 8);
                    mySuffix = Math.max(mySuffix, 0);
                    address[j] &= (byte)~((1<<mySuffix) - 1);
                }
                
        //        //Not Sure
        //        if (lastByteMin == 0)
        //            lastByteMin = 1;
        //        if (lastByteMax == 255)
        //            lastByteMax = 254;

                log.info("Going to Scan "+((1<<suffixMask0)-(lastScannedIPSuffix+1))+" Nodes " + InetAddress.getByAddress(address));
                try{
                    for (lastScannedIPSuffix++; lastScannedIPSuffix <= (1<<suffixMask0)-1; lastScannedIPSuffix++) {
                        for (int byteIndex = 0; byteIndex < 4; byteIndex++) {
                            int mySuffix = suffixMask0 - 8*(3-byteIndex);
                            mySuffix = Math.min(mySuffix, 8);
                            mySuffix = Math.max(mySuffix, 0);
                            address[byteIndex] = (byte) (
                                    (address[byteIndex] & ~((1<<mySuffix)-1))
                                    | (lastScannedIPSuffix>>8*(3-byteIndex) & ((1<<mySuffix)-1)));
                        }
                        try {
                            if (checkForServer(InetAddress.getByAddress(address))) {
                                return true;
                            }
                        } catch (UnknownHostException ex) {

                        }
                    }
                }
                finally{
                    log.info("Scan Completed for "+inetAddress);
                }
            }
        } catch (UnknownHostException ex) {
            log.info(ex.toString());
        }finally{
            log.info("Full Scan Completed!");
        }
        lastScannedIPSuffix = -1;
        return false;
    }
    
    /**
     * If Server is running on Given Address, Using TCP port
     * @param address
     * @return isRunning
     */
    private boolean checkForServer(InetAddress address) {
        boolean printStatus = true;
        if(printStatus)
            System.out.print("Checking "+address+" ");
        
        // Timeout for Checking 20ms
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(address, Configuration.PORT_FAST), 200);
            System.out.println("Connected");
            boolean working = false;
            if(socket.isConnected()) {
                if (fastScanEnabled) {
                    BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                    byte[] input = new byte[FAST_SCAN_MESSAGE.getBytes().length];

                    int len = bis.read(input);
                    if(len == FAST_SCAN_MESSAGE.getBytes().length) {
                        if(new String(input).equals(FAST_SCAN_MESSAGE)) {
                            System.out.print("> "+new String(input)+"< ");
                            working = true;
                        }
                    }
                } else
                    working = true;
                socket.close();
                if(printStatus && working)
                    System.out.println(" FAST_PORT ");
                if (working && addWorkingServer(address)) {
                    if(printStatus)
                        System.out.println("Working");
                    socket.close();
                    return true;
                }
            }
            socket.close();
        } catch (IOException ex) {
            if(printStatus)
                System.out.println("Exception : "+ex);    
        }
        if(printStatus)
            System.out.println();
        return false;
    }
    
    
    /**
     * Maintain Set of all discovered Server
     * @param address
     * @return isNotSkipped
     */
    private boolean addWorkingServer(InetAddress address) {
        log.info("Server "+address+" Found");
        
        lastScannedAddress = address;
        if(!serverFound.contains(address)) {
            serverFound.add(address);
        }
        return true;
    }

}
