/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import data.Messenger;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import server.utils.Config;

/**
 *
 * @author car team 16
 */
public class NameNodeManager extends Thread {
    private final boolean verbose;

    /**
     *
     * @param verbose
     */
    public NameNodeManager(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (this.verbose)
                    System.out.println("NameNodeManager: I'm alive");
                BuddyManager.sleep(2000);
            } catch (InterruptedException ex) {
                ex.printStackTrace(System.out);
            }
        }
    }

    public String update() {
        int port = Config.getInstance().getServerList().get(2).getServerPort();
        InetAddress address = Config.getInstance().getServerList().get(2).getAddress();
        try {
            Messenger messenger = new Messenger(new Socket(address, port));
            messenger.send("UPDATE");
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
        return "";
    }
    
}
