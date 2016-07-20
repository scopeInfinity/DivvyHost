package divvyhost.network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.rmi.ObjectSpace;
import divvyhost.configuration.Configuration;
import divvyhost.project.Details;
import divvyhost.project.Project;
import divvyhost.project.ProjectManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
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
    private String user;
    
    public DivvyClient(ProjectManager projectManager, String user) {
        this.projectManager = projectManager;
        this.user = user;
    }
    
    /**
     * Scan server if available on current network
     * @return isFound.
     */
    public boolean scanNetwork() {
        lastScannedAddress = null;
        try {
            //        Client client = new Client();
//        lastScannedAddress = client.discoverHost(Configuration.PORT_UCP, Configuration.PORT_SCAN_TIMEOUT);
            
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while(lastScannedAddress==null && networkInterfaces.hasMoreElements()){
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    if (checkServerOnSubnet(interfaceAddress.getAddress(), interfaceAddress.getNetworkPrefixLength())) {
                        log.info("Server Found on "+networkInterface.getDisplayName());
                        break;
                    }
                   
                }

            }
        } catch (SocketException ex) {
            log.severe(ex.toString());
        }
        
        if(lastScannedAddress == null) {
            log.info("No Server Found");
            return false;
        }
        log.info("Server["+lastScannedAddress.getHostAddress()+"] Found");
        return true;
    }

    /**
     * Connect if last find Server
     * @return isConnected
     */
    public boolean connect() {
        if(lastScannedAddress == null) {
            log.severe("No Scanned Host");
            return false;
        }
        client = new Client(8192,2048);
        NetworkRegister.register(client);
        divvyServer = ObjectSpace.getRemoteObject(client, NetworkRegister.RMI_SERVER, ServerInterface.class);
        
        client.start();
        try {
            client.connect(Configuration.CLIENT_CONNECT_TIMEOUT, lastScannedAddress, Configuration.PORT_TCP, Configuration.PORT_UCP);
            
            //Verifing Other Server
            try{
                if(divvyServer.isDivvyServer()) {
                    String otherUser = divvyServer.getUser();
                    if (otherUser!=null && !otherUser.equals(user)) {
                        return true;
                    }
                }
            }catch(Exception e) {
                log.severe("Invalid Client");
            }
            
            return true;
        } catch (IOException ex) {
            log.severe(ex.toString());
        }
        return false;
    }
    
    /**
     * Disconnect connection with server if connecter
     */
    public void disconnect() {
        if (client != null) {
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
     * Note : For now checking for max 254 nodes, on IPv4 Only
     * @param address
     * @param prefixLength
     * @return isServerFound
     */
    private boolean checkServerOnSubnet(InetAddress inetAddress, int prefixLength) {
        if (inetAddress.getAddress().length!=4) {
            // Only for IPv4
            return false;
        }
        
        //IPv4
        byte[] address = inetAddress.getAddress();
        int suffixMask0 = 32 - prefixLength;
        
        //Max Node 255.255.255.0
        suffixMask0 = Math.min(suffixMask0, 8);
        
        int lastByteMin = address[3], lastByteMax = address[3];
        lastByteMin = lastByteMin&~((1<<suffixMask0)-1);
        lastByteMax = lastByteMax|(1<<suffixMask0);
        
//        //Not Sure
//        if (lastByteMin == 0)
//            lastByteMin = 1;
//        if (lastByteMax == 255)
//            lastByteMax = 254;
        
        for (int lastByte = lastByteMin; lastByte < lastByteMax; lastByte++) {
            address[3] = (byte) lastByte;
            try {
                if (checkForServer(InetAddress.getByAddress(address))) {
                    return true;
                }
            } catch (UnknownHostException ex) {
                
            }
        }
        return false;
    }
    
    /**
     * If Server is running on Given Address, Using TCP port
     * @param address
     * @return isRunning
     */
    private boolean checkForServer(InetAddress address) {
        
        // Timeout for Checking 20ms
        try {
            if (!address.isReachable(20))
            return false;
            
            Socket socket = new Socket(address, Configuration.PORT_TCP);
            if(socket.isConnected()) {
                socket.close();
                lastScannedAddress = address;
                return true;
            }
            socket.close();
        } catch (IOException ex) {
            
        }
        return false;
    }
}
