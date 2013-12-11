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
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import server.utils.Config;
import server.utils.ServerData;
import server.utils.Status;

/**
 *
 * @author car team 16
 */
public class NameNode extends Thread {

    private final boolean verbose;
    private HashMap<String, List> theNode;

    private HashMap<String, List> theTmpNode;
    private boolean nodeIsInitializated;

    public NameNode(boolean verbose) {
        this.verbose = verbose;
        theNode = new HashMap<>();
        theTmpNode = new HashMap<>();
        nodeIsInitializated = false;
    }

    @Override
    public void run() {
    }

    public HashMap<String, List> getTheNode() {
        return theNode;
    }

    /**
     *
     * @param id
     * @return 1 if this id wasn't found, if not return 0
     */
    public int removeId(int id) {
        if (verbose) System.out.println("NameNode: removeId");
        int res = 1;
        for (Map.Entry<String, List> entry : theNode.entrySet()) {
            String string = entry.getKey();
            List<Integer> list = entry.getValue();
            if (list.remove(new Integer(id))) {
                if (verbose) System.out.println("NameNode: "+id+" removed");
                res = 0;
            }
        }
        return res;
    }

    int addId(int id) {
        if (verbose) System.out.println("NameNode: addId");
        int res = 1;
        for (Map.Entry<String, List> entry : theNode.entrySet()) {
            String string = entry.getKey();
            List<Integer> list = entry.getValue();
            if (!list.contains(new Integer(id))) {
                list.add(new Integer(id));
                if (verbose) System.out.println("NameNode: "+id+" added");
                res = 0;
            }
        }
        return res;
    }

    public void initializeTheNode() {
        if (verbose) System.out.println("NameNode: initializeTheNode");
        if (!nodeIsInitializated) {
            this.nodeIsInitializated = true;
            if (Config.getInstance().getStatut() == Status.SECONDARY) {
                        // if previous state was DATA we have to ask server for
                // is NameNode
                try {
                    this.copyNameNodeFrom(
                            Config.getInstance().getMaster().getAddress(),
                            Config.getInstance().getMaster().getServerPort());
                } catch (IOException ex) {
                    ex.printStackTrace(System.out);
                }
            } else if (Config.getInstance().IamTheMaster()) {
                // here we have to ask a secondary node since this one
                // wasn't initialized
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
                            this.copyNameNodeFrom(value.getAddress(),
                                    value.getServerPort());
                            break;
                        } catch (IOException ex) {
                            ex.printStackTrace(System.out);
                        }
                    }
                }
            }
        }
    }

    private void copyNameNodeFrom(InetAddress addr, int port) throws IOException {
        if (verbose) System.out.println("NameNode: copyNameNodeFrom "
                +addr.getHostAddress()+":"+port);
        Messenger messenger;
        messenger = new Messenger(new Socket(addr, port));
        messenger.send("GIVEMEMYNAMENODE");
        DataContainer receive = messenger.receive();
        data.NameNode nameNode;
        nameNode = (data.NameNode) receive.getData();
        this.theNode.putAll(nameNode.node);
        if (verbose) {
            System.out.println("Copying node...");
            for (Map.Entry<String, List> entry : theNode.entrySet()) {
                String string = entry.getKey();
                List list = entry.getValue();
                String res = "";
                for (int i = 0; i < list.size(); i++) {
                    res += list.get(i) + ", ";
                }
                System.out.println("NameNode create file \"" + string + "\" on servers: " + res);
            }
        }
    }

    public int create(String parameter, List<Integer> list) {
        if (verbose) System.out.println("NameNode: create "+parameter);
        if (theNode.containsKey(parameter)) {
            return 1;
        }
        theTmpNode.put(parameter, list);
        if (verbose) {
            String res = "";
            for (int i = 0; i < list.size(); i++) {
                res += list.get(i) + ", ";
            }
            System.out.println("NameNode create file \"" + parameter + "\" on servers: " + res);
        }
        return 0;
    }

    public void deliver(String parameter) {
        if (theTmpNode.containsKey(parameter)) {
            theNode.put(parameter, theTmpNode.get(parameter));
            theTmpNode.remove(parameter);
        }
        if (verbose) {
            System.out.println("NameNode deliver \"" + parameter + "\"");
        }
    }

    public IdList getIds(String parameter) {
        if (verbose) System.out.println("NameNode: getIds "+parameter);
        if (theNode.containsKey(parameter)) {
            IdList idList = new IdList();
            idList.list.addAll(theNode.get(parameter));
            return idList;
        }
        return null;
    }

    void delete(String parameter) {
        if (verbose) System.out.println("NameNode: delete "+parameter);
        theNode.remove(parameter);
    }
}
