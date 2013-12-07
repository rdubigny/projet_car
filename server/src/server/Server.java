/*
 * Here is the main for a server instance. 
 * It start all the services needed to run the distributed file system.
 */
package server;

import java.net.InetAddress;
import java.net.ServerSocket;
import server.utils.*;

/**
 *
 * @author car team 16
 */
public class Server {
    static final int maxConnections = 20;

    public static BuddyManager buddyManager;
    public static NameNodeManager nameNodeManager;
    public static NameNode nameNode;
    
    /**
     * @param args the only argument is the id of this server within the 
     * config.txt configuration file
     */
    public static void main(String[] args) {
        System.out.println("Server started!");
        try {
            
            if (args.length == 0 | args.length > 1){
                throw new Exception("Invalid parameters");
            }
            // initialize the config static class
            System.out.println("Reading config file...");
            ConfigReader.readConfigFile(Integer.parseInt(args[0]));
            System.out.println("Reading config file... DONE");
            
            System.out.println("Launching services...");
            // launch the main services as threads here
            buddyManager = new BuddyManager(false);
            buddyManager.start();
            System.out.println("BuddyManager launched!");
            
            nameNodeManager = new NameNodeManager(false);
            nameNodeManager.start();
            System.out.println("NameNodeManager launched!");
            nameNode = new NameNode(false);
            nameNode.start();
            System.out.println("NameNode launched!");
            DataNodeManager dataNodeManager = new DataNodeManager(false);
            dataNodeManager.start();
            System.out.println("dataNodeManager launched!");
            DataNode dataNode = new DataNode(false);
            dataNode.start();
            System.out.println("dataNode launched!");
            ServerSocket ss = new ServerSocket(
                    Config.getInstance().getThisServer().getClientPort(), 
                    maxConnections, InetAddress.getByName(null));
            while (true) {
                ClientRequestManager clientRequestManager = new ClientRequestManager(ss.accept());
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        } finally {
            System.out.println("Launching services...DONE");
        }
    }
}
