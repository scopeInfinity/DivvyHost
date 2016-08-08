package divvyhost.network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.rmi.ObjectSpace;
import divvyhost.configuration.Configuration;
import static divvyhost.configuration.Configuration.FAST_SCAN_MESSAGE;
import static divvyhost.configuration.Configuration.fastScanEnabled;
import divvyhost.project.Details;
import divvyhost.project.Project;
import divvyhost.project.ProjectManager;
import divvyhost.utils.Utils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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
    
    private InetAddress lastScannedAddress;
    private Client client;
    private ServerInterface divvyServer;
    
    private ProjectManager projectManager; 
    private Configuration configuration;
    private String user;
    
    private Set<InetAddress> serverDone;
    private int lastScannedIPListIndex;
    private int lastScannedIPSuffix;
            
    public DivvyClient(ProjectManager projectManager, String user) {
        this.projectManager = projectManager;
        this.user = user;
        serverDone = new HashSet<InetAddress>();
        configuration = new Configuration();
            
    }
    
    private void initClient() {
        lastScannedIPListIndex = 0;
        lastScannedIPSuffix = -1;
        client = new Client(Configuration.BUFFER_SIZE_CLIENT1, Configuration.BUFFER_SIZE_CLIENT2);
        NetworkRegister.register(client);
    }
     
    private void getRemoteObj() {
        divvyServer = ObjectSpace.getRemoteObject(client, NetworkRegister.RMI_SERVER, ServerInterface.class);
    }
    
    /**
     * Scan server if available on current network
     * @return isFound.
     */
    public boolean scanNetwork() {
        InetAddress lastScanIP = lastScannedAddress;
        lastScannedIPListIndex = 0;
        lastScannedIPSuffix = -1;
        lastScannedAddress = null;
        firstScannedServer = null;
        isLastFreshServer = false;
        
//        try {
//              Client client = new Client();
//        lastScannedAddress = client.discoverHost(Configuration.PORT_UCP, Configuration.PORT_SCAN_TIMEOUT);
            
            //Scanning Network Cards
//            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
//            while(lastScannedAddress==null && networkInterfaces.hasMoreElements()){
//                NetworkInterface networkInterface = networkInterfaces.nextElement();
//                
//                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
//                    if (checkServerOnSubnet(interfaceAddress.getAddress(), interfaceAddress.getNetworkPrefixLength())) {
//                        log.info("Server Found on "+networkInterface.getDisplayName());
//                        break;
//                    }
//                   
//                }
//
//            }
//        } catch (SocketException ex) {
//            log.severe(ex.toString());
//        }
        
        //Using Configuration File
       if (checkServerOnSubnet(configuration.getInternalIPs(), configuration.getPrefixLengths(), lastScanIP)) {
           log.info("Server Found");
       }           
        if(firstScannedServer == null) {
            log.info("No Server Found");
            return false;
        }
        
        if(lastScannedAddress == null) {
            log.info("No New Server Found, Using already scanned one ["+firstScannedServer+"]");
            lastScannedAddress = firstScannedServer;
            serverDone.clear();
            serverDone.add(lastScannedAddress);
        }
        
        log.info("Server["+lastScannedAddress.getHostAddress()+"] Found");
        return true;
    }
    
    private boolean isLastFreshServer;
    /**
     * Return if Last Scanned Server is New
     * @return isNew
     */
    public boolean isLastFreshServer() {
        return isLastFreshServer;
    }

    public void makeServerHistoryClear() {
        serverDone.clear();
        isLastFreshServer = false;
        lastScannedAddress = null;
        firstScannedServer = null;
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
            if (client==null) {
                initClient();
            }
            client.start();
            client.connect(Configuration.CLIENT_CONNECT_TIMEOUT, lastScannedAddress, Configuration.PORT_TCP);
            getRemoteObj();
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
            if(client!=null) {
                client.close();
                client.stop();
            }
        }
        return false;
    }
    
    /**
     * Disconnect connection with server if connecter
     */
    public void disconnect() {
        if (client != null) {
            client.stop();
            client.close();
        }
    }

    /**
     * Request Server for project Update
     */
    public void sync() {
        List<Details> othersList = divvyServer.listDetails();
        log.info("Number of Project of Server["+lastScannedAddress.getHostAddress()+"] "+othersList.size());
        List<Details> fetchThese = projectManager.processOtherClientList(othersList);
        log.info("Number of New Project of Server["+lastScannedAddress.getHostAddress()+"] "+fetchThese.size());
        
        for (Details projectDetails : fetchThese) {
            
            Project newProject = divvyServer.getProject(projectDetails.getpID().toString());
            if(newProject == null)
            {
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
        boolean printStatus = false;
        if(printStatus)
            System.out.print("Checking "+address+" ");
        
        // Timeout for Checking 20ms
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(address, Configuration.PORT_FAST), 100);
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
    
    
    private InetAddress firstScannedServer;
    /**
     * Maintain List if Server is Already Scanned, it skips it
     * @param address
     * @return isNotSkipped
     */
    private boolean addWorkingServer(InetAddress address) {
        log.info("Server "+address+" Found");
        if (firstScannedServer == null)
            firstScannedServer = address;
        
        if(!serverDone.contains(address)) {
            lastScannedAddress = address;
            isLastFreshServer = true;
            serverDone.add(address);
            return true;
        }
        return false;
    }
}
