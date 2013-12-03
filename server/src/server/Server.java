/*
 * Here is the main for a server instance. 
 * It start all the services needed to run the distributed file system.
 */
package server;

import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.utils.ConfigReader;

/**
 *
 * @author car team 16
 */
public class Server {
    static final int maxConnections = 20;
    static LinkedList<Address> adresses; // list of the other server datas
    static Address myAddress; // data of this server

    /**
     * @param args the argument will be about ip and port
     */
    public static void main(String[] args) {
        try {
            // generate localhost inet address
            InetAddress tmpAddr;
            tmpAddr = InetAddress.getByName(null); // resolve into localhost

            // initialize datas for this server
            myAddress = new Address(tmpAddr, 10100, State.DATA);
            adresses = new LinkedList();
            // intitialize datas for other servers
            boolean added;
            added = adresses.add(new Address(tmpAddr, 10000, State.DATA));
            added = adresses.add(new Address(tmpAddr, 10200, State.DATA));
            
            //ConfigReader.readConfigFile(Integer.parseInt(args[0]));

            // launch the main services as threads here
//             BuddyManager buddyManager = new BuddyManager();
  //           buddyManager.start();
             /*NameNodeManager nameNodeManager = new NameNodeManager();
             nameNodeManager.start();
             NameNode nameNode = new NameNode();
             nameNode.start();
             DataNodeManager dataNodeManager = new DataNodeManager();
             DataNode dataNode = new DataNode();*/
            
            ServerSocket ss = new ServerSocket(10300, maxConnections, InetAddress.getByName(null));
            while (true) {
                new ClientRequestManager(ss.accept());
            }
        } catch (UnknownHostException ex) {
            // exception occures if inet address can't be resolved
            System.out.println(ex.getStackTrace());
        } catch (IOException ex) {
            // occures if their were an error with the buddy sockets
            System.out.println(ex.getStackTrace());
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
