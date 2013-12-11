/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import data.Data;
import data.DataContainer;
import data.IdList;
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
    private final Object searchSecondaryLock;
    private final Object RegisterLock;
    private final Object allHaveRespLock;
    private int respCounter;
    private final Object activateNameNodeManager;

    /**
     *
     * @param verbose
     */
    public NameNodeManager(boolean verbose) {
        this.searchSecondaryLock = new Object();
        this.RegisterLock = new Object();
        this.allHaveRespLock = new Object();
        executor = Executors.newCachedThreadPool();
        this.verbose = verbose;
        this.activateNameNodeManager = new Object();
    }

    @Override
    public void run() {
    }

    /**
     * make sure they are enough secondary servers. If not, launch new secondary
     * election.
     */
    void check() {
        if (verbose) System.out.println("NameNodeManager: check");
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
        if (nbSec < Server.K) {
            this.electNewSecondary(Server.K - nbSec);
        }
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
            if (verbose) {
                System.out.println("NameNodeManager: looking for "
                        + this.secondaryToFound + " secondary...");
            }
            synchronized (searchSecondaryLock) {
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
                            System.out.println("NameNodeManager: new secondaryfound: " + key);
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
            if (verbose) {
                if (this.secondaryToFound == 0) {
                    System.out.println("NameNodeManager: All secondary have been found");
                } else {
                    System.out.println("NameNodeManager: " + this.secondaryToFound
                            + " secondary are still missing");
                }
            }
        }
    }

    /**
     * Find eligible server to store the file. Register it on all nameNode.
     *
     * @param parameter
     * @return
     */
    public IdList register(String parameter) {
        if (verbose) System.out.println("NameNodeManager: register "+parameter);
        IdList idList = new IdList();
        int size = Config.getInstance().getServerList().size();
        int thisId = Config.getInstance().getThisServer().getId();
        // send back random ids
        int id;
        // if to many servers are down, the loop will run for ever
        // to prevent that we introduce maxTries
        int maxTries = Server.K * 100;
        do {
            id = (int) (Math.random() * (size + 1)); // +1 for this server
            if (!idList.list.contains(id)) {
                if (id == thisId) {
                    idList.list.add(id);
                } else {
                    if (Config.getInstance().getServerList().get(id).getStatus()
                            != Status.DOWN) {
                        idList.list.add(id);
                    }
                }
            }
            maxTries--;
        } while (idList.list.size() < Server.K + 1 && maxTries > 0);
        if (idList.list.size() < Server.K + 1) {
            idList.list.clear();
            return idList;
        } else {
            IdList returnValue = null;
            synchronized (RegisterLock) {
                DataContainer resp = new DataContainer("CREATEUPDATE", parameter, (Data) idList);
                this.respCounter = Server.K + 1;
                HashMap<Integer, ServerData> list;
                list = Config.getInstance().getServerList();
                Iterator<Integer> itr;
                itr = list.keySet().iterator();
                while (itr.hasNext()) {
                    Integer key = itr.next();
                    ServerData value;
                    value = list.get(key);
                    if (value.getStatus() == Status.SECONDARY) {
                        executor.submit(new registerThread(value.getAddress(),
                                value.getServerPort(), resp));
                    }
                }
                executor.submit(new registerThread(
                        Config.getInstance().getThisServer().getAddress(),
                        Config.getInstance().getThisServer().getServerPort(),
                        resp));
                synchronized (allHaveRespLock) {
                    try {
                        allHaveRespLock.wait(3 * 1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace(System.out);
                    }
                    if (respCounter <= 0) {
                        returnValue = idList;
                    }
                }
            }
            return returnValue;
        }
    }

    /**
     * write in remote nameNode
     */
    private class registerThread extends Thread {

        private final InetAddress addr;
        private final int port;
        private final DataContainer resp;

        public registerThread(InetAddress addr, int port, DataContainer resp) {
            this.addr = addr;
            this.port = port;
            this.resp = resp;
        }

        @Override
        public void run() {
            try {
                Messenger messenger = new Messenger(new Socket(addr, port));
                // if the server was found, keep going
                messenger.send(resp);
                DataContainer request = messenger.receive();
                String content = request.getContent();
                if (content.equals("OK")) {
                    messenger.send("DELIVER");
                    messenger.close();
                    synchronized (allHaveRespLock) {
                        respCounter--;
                        if (respCounter <= 0) {
                            allHaveRespLock.notify();
                        }
                    }
                } else if (content.equals("KO")) {
                    messenger.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.out);
            }
        }

    }

    /**
     * unregister the file amongst the secondary servers
     *
     * @param parameter
     */
    void unregister(String parameter) {
        if (verbose) System.out.println("NameNodeManager: unregister "+parameter);
        DataContainer resp = new DataContainer("REMOVEUPDATE", parameter);
        HashMap<Integer, ServerData> list;
        list = Config.getInstance().getServerList();
        Iterator<Integer> itr;
        itr = list.keySet().iterator();
        while (itr.hasNext()) {
            Integer key = itr.next();
            ServerData value;
            value = list.get(key);
            if (value.getStatus() == Status.SECONDARY) {
                executor.submit(new unRegisterThread(value.getAddress(),
                        value.getServerPort(), resp));
            }
        }
        executor.submit(new unRegisterThread(
                Config.getInstance().getThisServer().getAddress(),
                Config.getInstance().getThisServer().getServerPort(),
                resp));
    }

    private class unRegisterThread extends Thread {

        private final InetAddress addr;
        private final int port;
        private final DataContainer resp;

        public unRegisterThread(InetAddress addr, int port, DataContainer resp) {
            this.addr = addr;
            this.port = port;
            this.resp = resp;
        }

        @Override
        public void run() {
            try {
                Messenger messenger = new Messenger(new Socket(addr, port));
                // if the server was found, keep going
                messenger.send(resp);
                messenger.close();
            } catch (IOException ex) {
                ex.printStackTrace(System.out);
            }
        }
    }    
}
