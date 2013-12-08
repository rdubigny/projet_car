/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import data.Messenger;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import server.utils.Config;
import server.utils.ServerData;
import server.utils.Status;

/**
 *
 * @author car team 16
 */
public class NameNodeManager extends Thread {

    private final boolean verbose;
    private final ExecutorService executor;
    private final Object lock;

    /**
     *
     * @param verbose
     */
    public NameNodeManager(boolean verbose) {
        this.lock = new Object();
        executor = Executors.newCachedThreadPool();
        this.verbose = verbose;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (Config.getInstance().masterIdChange) {
                try {
                    Config.getInstance().masterIdChange.wait();
                    if (Config.getInstance().IamTheMaster()) {
                        // we have to wait here because this server must learn 
                        // the servers who already are secondary first
                        sleep(2 * 1000);
                        // now count all recorded secondary and launch an election if needed
                        if (verbose) {
                            System.out.println("NameNodeManager activated!");
                        }
                        int nbSec = 0;
                        HashMap<Integer, ServerData> list;
                        list = Config.getInstance().getServerList();
                        Iterator<Integer> itr;
                        itr = list.keySet().iterator();
                        while (itr.hasNext()) {
                            Integer key = itr.next();
                            ServerData value;
                            value = list.get(key);
                            if (value.getStatus() == Status.SECONDARY) {
                                nbSec++;
                            }
                        }
                        // if there is not enought secondary, launch an election
                        if (nbSec < Server.K){
                            this.electNewSecondary(Server.K-nbSec);
                        }
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace(System.out);
                }
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

    public void electNewSecondary(int secDown) {
        executor.submit(new electNewSecondaryThread(secDown));
    }

    private class electNewSecondaryThread implements Runnable {

        int secondaryToFound;

        private electNewSecondaryThread(int secDown) {
            this.secondaryToFound = secDown;
        }

        @Override
        public void run() {
            while (this.secondaryToFound > 0) { // will run until enought secondary are found
                if (verbose) {
                    System.out.println("looking for "
                            + this.secondaryToFound + " secondary...");
                }
                synchronized(lock){
                    HashMap<Integer, ServerData> list;
                    list = Config.getInstance().getServerList();
                    Iterator<Integer> itr;
                    itr = list.keySet().iterator();
                    while (itr.hasNext() && this.secondaryToFound > 0) {
                        Integer key = itr.next();
                        ServerData value;
                        value = list.get(key);
                        if (value.getStatus() == Status.DATA) {
                            if (verbose) {
                                System.out.println("New secondaryfound: "+key);
                            }
                            this.secondaryToFound--;
                            try {
                                Messenger messenger;
                                messenger = new Messenger(
                                        new Socket(value.getAddress(),
                                                value.getServerPort()));
                                messenger.send("SECONDARY");
                                value.setStatus(Status.SECONDARY);
                                messenger.close();
                            } catch (IOException ex) {
                                ex.printStackTrace(System.out);
                            }
                        }
                    }
                }
                if (this.secondaryToFound > 0) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace(System.out);
                    }
                }
            }
        }
    }

}
