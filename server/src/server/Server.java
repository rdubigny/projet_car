/*
 * Here is the main for a server instance. 
 * It start all the services needed to run the distributed file system.
 */
package server;

import java.io.IOException;
import java.net.*;
import server.utils.ConfigReader;

/**
 *
 * @author car team 16
 */
public class Server {
    static final int maxConnections = 20;
    //static LinkedList<Address> adresses; // list of the other server datas
    //static Address myAddress; // data of this server

    /**
     * @param args the argument will be about ip and port
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
            BuddyManager buddyManager = new BuddyManager();
            buddyManager.start();
            /*NameNodeManager nameNodeManager = new NameNodeManager();
            nameNodeManager.start();
            NameNode nameNode = new NameNode();
            nameNode.start();
            DataNodeManager dataNodeManager = new DataNodeManager();
            DataNode dataNode = new DataNode();/
            
            ServerSocket ss = new ServerSocket(10300, maxConnections, InetAddress.getByName(null));
            while (true) {
                new ClientRequestManager(ss.accept());
            }*/
        } catch (UnknownHostException ex) {
            // exception occures if inet address can't be resolved
            System.out.println(ex.getStackTrace());
        } catch (IOException ex) {
            // occures if their were an error with the buddy sockets
            System.out.println(ex.getStackTrace());
        } catch (Exception ex) {
            System.out.println(ex.getStackTrace());
        } finally {
            System.out.println("Server Stopped!");
        }
    }
}
