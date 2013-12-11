package server;

import data.Archive;
import data.Data;
import data.DataContainer;
import data.Messenger;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import server.utils.Config;
import server.utils.ServerData;
import server.utils.Status;

public class DataNodeManager extends Thread {

    private final boolean verbose;
    private final ExecutorService executor;

    public DataNodeManager(boolean verbose) {
        this.verbose = verbose;
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
    }

    /**
     * detect the down servers and make sure they are not in the nameNode
     * anymore
     */
    public void check() {
        if (verbose) {
            System.out.println("DataNodeManager: check");
        }
        // detect the down servers
        HashMap<Integer, ServerData> list;
        list = Config.getInstance().getServerList();
        Iterator<Integer> itr;
        itr = list.keySet().iterator();
        while (itr.hasNext()) {
            Integer key = itr.next();
            ServerData value;
            value = list.get(key);
            if (value.getStatus() == Status.DOWN) {
                // remove id from all nameNodes
                this.removeId(key);
            }
        }
    }

    public void removeId(int id) {
        if (verbose) {
            System.out.println("DataNodeManager: removeId ");
        }
        if (Server.nameNode.removeId(id) == 0) {
            if (verbose) {
                System.out.println("DataNodeManager: removing " + id);
            }
            HashMap<Integer, ServerData> list;
            list = Config.getInstance().getServerList();
            Iterator<Integer> itr;
            itr = list.keySet().iterator();
            while (itr.hasNext()) {
                Integer key = itr.next();
                ServerData value;
                value = list.get(key);
                if (value.getStatus() == Status.SECONDARY) {
                    try {
                        Messenger messenger;
                        messenger = new Messenger(
                                new Socket(value.getAddress(),
                                        value.getServerPort()));
                        DataContainer rq = new DataContainer("REMOVEID", String.valueOf(id));
                        messenger.send(rq);
                        messenger.close();
                    } catch (IOException ex) {
                        if (verbose) {
                            System.out.println("DataNodeManager: a secondary seems dead");
                        }
                        ex.printStackTrace(System.out);
                    }
                }
            }
        }
        this.copyLauncher();
    }

    public void addId(int id) {
        if (verbose) {
            System.out.println("DataNodeManager: addId ");
        }
        if (Server.nameNode.addId(id) == 0) {
            if (verbose) {
                System.out.println("DataNodeManager: add " + id);
            }
            HashMap<Integer, ServerData> list;
            list = Config.getInstance().getServerList();
            Iterator<Integer> itr;
            itr = list.keySet().iterator();
            while (itr.hasNext()) {
                Integer key = itr.next();
                ServerData value;
                value = list.get(key);
                if (value.getStatus() == Status.SECONDARY) {
                    try {
                        Messenger messenger;
                        messenger = new Messenger(
                                new Socket(value.getAddress(),
                                        value.getServerPort()));
                        DataContainer rq = new DataContainer("ADDID", String.valueOf(id));
                        messenger.send(rq);
                        messenger.close();
                    } catch (IOException ex) {
                        if (verbose) {
                            System.out.println("DataNodeManager: a secondary seems dead");
                        }
                        ex.printStackTrace(System.out);
                    }
                }
            }
        }
    }

    private void copyLauncher() {
        if (verbose) {
            System.out.println("DataNodeManager: copyLauncher");
        }
        HashMap<String, List> theNode;
        theNode = Server.nameNode.getTheNode();
        Iterator<String> itr = theNode.keySet().iterator();
        while (itr.hasNext()) {
            String fileName = itr.next();
            // if there is not enought copy of this file try to launch copying thread
            if (theNode.get(fileName).size() <= Server.K) {
                if (verbose) {
                    System.out.println("DataNodeManager: copies are needed for " + fileName);
                    this.findDataServerToCopy(fileName, theNode.get(fileName));
                }
            }
        }
    }

    private void findDataServerToCopy(String fileName, List list) {
        System.out.println("DataNodeManager: findDataServerToCopy");
        // first select a data server amongst the servers available
        Integer selectedServerId = null;
        HashMap<Integer, ServerData> serverList = new HashMap<>();
        serverList.putAll(Config.getInstance().getServerList());
        serverList.put(Config.getInstance().getThisServer().getId(),
                Config.getInstance().getThisServer());
        Iterator<Integer> itr;
        itr = serverList.keySet().iterator();
        while (itr.hasNext() && selectedServerId == null) {
            Integer key = itr.next();
            ServerData value;
            value = serverList.get(key);
            if (value.getStatus() != Status.DOWN
                    && !list.contains(key)) {
                selectedServerId = new Integer(key);
            }
        }
        if (verbose) {
            if (selectedServerId != null) {
                System.out.println("DataNodeManager: server " + selectedServerId
                        + " sounds good for a copy of " + fileName);
            } else {
                System.out.println("DataNodeManager: no servers founds for copying " + fileName);
            }
        }
        // if a server was found
        if (selectedServerId != null) {
            // register the copy
            this.addId(selectedServerId);
            // copy the file
            Server.dataNode.copyTo(selectedServerId, fileName);
        }
    }
}
