package server;

import data.Archive;
import data.Data;
import data.DataContainer;
import data.Messenger;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.Socket;
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

    public void copyData(int id) {
        if (verbose) {
            System.out.println("dataNodeManager.copyingData");
        }
        HashMap<String, List> oldNode = new HashMap<>();
        oldNode.putAll(Server.nameNode.getTheNode());
        Server.nameNode.removeId(id);
        // remove id from all tables
        if (verbose) {
            System.out.println("cleaning tables...");
        }
        HashMap<Integer, ServerData> list;
        list = Config.getInstance().getServerList();
        Iterator<Integer> itr;
        itr = list.keySet().iterator();
        while (itr.hasNext()) {
            Integer key = itr.next();
            ServerData value;
            value = list.get(key);
            if (value.getStatus() == Status.SECONDARY
                    || value.getStatus() != Status.DATA) {
                try {
                    Messenger messenger;
                    messenger = new Messenger(
                            new Socket(value.getAddress(),
                                    value.getServerPort()));
                    DataContainer rq = new DataContainer("REMOVEID", String.valueOf(id));
                    messenger.send(rq);
                    messenger.close();
                } catch (IOException ex) {
                    ex.printStackTrace(System.out);
                }
            }
        }

        if (verbose) {
            System.out.println("Launching thread...");
        }
        executor.submit(new copyDataThread(id, oldNode));
    }

    private class copyDataThread implements Runnable {

        int id;
        HashMap<String, List> oldNode;

        private copyDataThread(int id, HashMap<String, List> oldNode) {
            this.id = id;
            this.oldNode = oldNode;
        }

        @Override
        public void run() {
            if (verbose) {
                System.out.println("Launching thread...DONE");
            }
            List<String> entriesToRemove = new LinkedList<>();
            entriesToRemove.clear();
            for (Map.Entry<String, List> entry : oldNode.entrySet()) {
                String fileName = entry.getKey();
                List ids = entry.getValue();
                if (!ids.contains(id)) {
                    entriesToRemove.add(fileName);
                }
            }
            for (int i = 0; i < entriesToRemove.size(); i++) {
                oldNode.remove(entriesToRemove.get(i));
            }
                // will run until all copy are done
            // removing non concerned entries
            if (verbose) {
                System.out.println("looking for copying " + this.oldNode.size() + " files...");
            }

            for (Map.Entry<String, List> entry : oldNode.entrySet()) {
                String fileName = entry.getKey();
                List ids = entry.getValue();
                boolean oneMoreFound = false;
                while (!oneMoreFound) {
                    HashMap<Integer, ServerData> list;
                    list = Config.getInstance().getServerList();
                    Iterator<Integer> itr;
                    itr = list.keySet().iterator();
                    while (itr.hasNext()) {
                        Integer key = itr.next();
                        ServerData value;
                        value = list.get(key);
                        if (value.getStatus() != Status.DOWN
                                && (!ids.contains(key) || key == id)) {
                            try {
                                Messenger messenger;
                                messenger = new Messenger(
                                        new Socket(value.getAddress(),
                                                value.getServerPort()));

                                Archive archive = new Archive(fileName, null);
                                DataContainer dataC;
                                dataC = new DataContainer("COPYTO", String.valueOf(key), (Data) archive);
                                messenger.send(dataC);
                                messenger.close();
                                oneMoreFound = true;
                                break;
                            } catch (IOException ex) {
                                ex.printStackTrace(System.out);
                            }
                        }
                    }
                }
            }
        }
    }

}
