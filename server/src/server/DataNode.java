package server;

import data.Archive;
import data.ArchiveAndList;
import data.Data;
import data.DataContainer;
import data.Messenger;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import memory.Memory;
import server.utils.Config;
import server.utils.ServerData;

public class DataNode extends Thread {

    private final boolean verbose;
    public Memory memory;
    private final ExecutorService executor;

    public DataNode(boolean verbose) {
        this.verbose = verbose;
        memory = new Memory();
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
    }

    void copyTo(int id, String fileName) {
        if (verbose) System.out.println("DataNode: "+fileName+" copyTo "+id);
        Archive archive = memory.mem.get(fileName);
        executor.submit(new writeMultipleThread((Data) archive, id));
    }

    void writeMultiple(Data data) {
        if (verbose) System.out.println("DataNode: writeMultiple");
        ArchiveAndList fData = (ArchiveAndList) data;
        Archive archive = fData.archive;
        List<Integer> list = fData.list;
        executor.submit(new writeSimpleThread((Data) archive));
        for (int i = 0; i < list.size(); i++) {
            int id = list.get(i);
            executor.submit(new writeMultipleThread((Data) archive, id));
        }
    }

    private class writeMultipleThread implements Runnable {

        Data data;
        int id;

        public writeMultipleThread(Data data, int id) {
            this.data = data;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                ServerData value = Config.getInstance().getServerList().get(id);
                Messenger messenger;
                messenger = new Messenger(
                        new Socket(value.getAddress(),
                                value.getServerPort()));
                DataContainer resp = new DataContainer("WRITE", data);
                messenger.send(resp);
                messenger.close();
            } catch (IOException ex) {
                ex.printStackTrace(System.out);
            }
        }
    }

    void writeSimple(Data data) {
        if (verbose) System.out.println("DataNode: writeSimple");
        executor.submit(new writeSimpleThread(data));
    }

    private class writeSimpleThread implements Runnable {

        Archive archive;

        public writeSimpleThread(Data data) {
            this.archive = (Archive) data;
        }

        @Override
        public void run() {
            System.out.println("CREATE A FILE");
            System.out.println(archive.getFileName());
            Server.dataNode.memory.mem_tmp.put(archive.getFileName(), archive);
            try {
                ServerData master = Config.getInstance().getMaster();
                Messenger messenger;
                messenger = new Messenger(
                        new Socket(master.getAddress(),
                                master.getServerPort()));
                DataContainer resp = new DataContainer("WRITEOK", archive.getFileName());
                messenger.send(resp);
                DataContainer masterResp;
                masterResp = messenger.receive();
                if (masterResp == null) {
                    Server.dataNode.memory.mem_tmp.remove(archive.getFileName());
                } else if (masterResp.getContent().equals("DELIVER")) {
                    Server.dataNode.memory.mem.put(archive.getFileName(), archive);
                    Server.dataNode.memory.mem_tmp.remove(archive.getFileName());
                }
                messenger.close();
            } catch (IOException ex) {
                ex.printStackTrace(System.out);
            }
        }
    }
}
