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

    public NameNode(boolean verbose) {
        this.verbose = verbose;
        theNode = new HashMap<>();
        theTmpNode = new HashMap<>();
    }

    public HashMap<String, List> getTheNode() {
        return theNode;
    }
    
    public void removeId(int id){
        for (Map.Entry<String, List> entry : theNode.entrySet()) {
            String string = entry.getKey();
            List list = entry.getValue();
            list.remove(id);            
        }
    }

    @Override
    public void run() {
        while (true) {
            synchronized (Config.getInstance().lockStatus) {
                try {
                    Status svgStatus = Config.getInstance().getStatut();
                    Config.getInstance().lockStatus.wait();
                    if (Config.getInstance().getStatut() == Status.SECONDARY
                            && svgStatus == Status.DATA) {
                        // if previous state was DATA we have to ask server for
                        // is NameNode
                        try {
                            this.initializeTheNode(
                                Config.getInstance().getMaster().getAddress(),
                                Config.getInstance().getMaster().getServerPort());
                        } catch (IOException ex) {
                            ex.printStackTrace(System.out);
                        }
                    } else if (Config.getInstance().IamTheMaster()
                            && svgStatus == Status.DATA) {
                        // here we have to ask a secondary node since this one
                        // wasn't initialized
                        // we have to wait here because this server must learn 
                        // the servers status first
                        sleep(2 * 1000);
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
                                    this.initializeTheNode(value.getAddress(),
                                            value.getServerPort());
                                    break;
                                } catch (IOException ex) {
                                    ex.printStackTrace(System.out);
                                }
                            }
                        }
                    }
                    if (verbose) {
                        System.out.println("NameNode activated!");
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace(System.out);
                }
            }
        }
    }

    private void initializeTheNode(InetAddress addr, int port) throws IOException {
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

    /**
     *
     * @return zero if the process finished successfully
     */
    public int update() {
        System.out.println("nameNode.update");
        // TODO : update NameNode
        return 0;
    }

    public int create(String parameter, List<Integer> list) {
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
        if (theNode.containsKey(parameter)) {
            IdList idList = new IdList();
            idList.list.addAll(theNode.get(parameter));
            return idList;
        }
        return null;
    }

    void delete(String parameter) {
        theNode.remove(parameter);
    }
}
