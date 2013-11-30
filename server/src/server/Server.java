/*
 * Here is the main for a server instance. 
 * It start all the services needed to run the distributed file system.
 */
package server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author car team 16
 */
public class Server {

    /**
     * @param args the argument will be about ip and port
     */
    public static void main(String[] args) {
        try {
            // launch the main services as threads here
            BuddyManager buddyManager = new BuddyManager();
            buddyManager.start();/*
            NameNodeManager nameNodeManager = new NameNodeManager();
            nameNodeManager.start();
            NameNode nameNode = new NameNode();
            nameNode.start();
            DataNodeManager dataNodeManager = new DataNodeManager();
            DataNode dataNode = new DataNode();*/
        } catch (IOException ex) {
            System.out.println(ex.getStackTrace());
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
