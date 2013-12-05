/*
 * Here is the main for a server instance. 
 * It start all the services needed to run the distributed file system.
 */
package server;

import java.net.InetAddress;
import java.net.ServerSocket;
import server.utils.ConfigReader;

/**
 *
 * @author car team 16
 */
public class Server {
    static final int maxConnections = 20;

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
            BuddyManager buddyManager = new BuddyManager(true);
            buddyManager.start();
            System.out.println("BuddyManager launched!");
            NameNodeManager nameNodeManager = new NameNodeManager(false);
            nameNodeManager.start();
            System.out.println("NameNodeManager launched!");
            NameNode nameNode = new NameNode(false);
            nameNode.start();
            System.out.println("NameNode launched!");
            DataNodeManager dataNodeManager = new DataNodeManager();
            DataNode dataNode = new DataNode();
            ServerSocket ss = new ServerSocket(10300, maxConnections, InetAddress.getByName(null));
            while (true) {
                new ClientRequestManager(ss.accept());
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        } finally {
            System.out.println("Launching services...DONE");
        }
    }
}
