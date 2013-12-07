/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import server.utils.Config;

/**
 *
 * @author scar
 */
public class ConnectionListener extends Thread {

    private final int maxConnections = 10;
    private final int port;
    private final boolean listenToServer;
    private ServerSocket ss; 

    /**
     * 
     * @param listenToServer if true, configure the listener to communicate with 
     * other servers, else to communicate with clients
     */
    ConnectionListener(boolean listenToServer) {
        this.listenToServer = listenToServer;
        if (listenToServer) {
            this.port = Config.getInstance().getThisServer().getServerPort();
        } else {
            this.port = Config.getInstance().getThisServer().getClientPort();
        }
        try {
            ss = new ServerSocket(this.port,
                    this.maxConnections, InetAddress.getByName(null));
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (listenToServer) {
                    new ServerRequestManager(this.ss.accept());
                } else {
                    new ClientRequestManager(this.ss.accept());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
    }

}
