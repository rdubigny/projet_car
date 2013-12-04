/*
 * BuddyManager implement the buddy algorithm for this server. It also maintain 
 * a list of the servers and their respective roles.
 */
package server;

import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import server.utils.Config;
import server.utils.ServerData;

/**
 *
 * @author car team 16
 */
public class BuddyManager extends Thread {

    private final DatagramSocket serverSocket; // UDP socket

    // private final MulticastSocket multiServerSocket; // UDP multicast socket
    private AtomicBoolean IamProposing;
    private AtomicBoolean WaitingForMasterAnswer;
    private final ExecutorService executor;
    
    private final boolean verbose;

    /**
     * Initialize socket
     *
     * @param verbose
     * @throws IOException
     */
    public BuddyManager(boolean verbose) throws IOException {
        this.verbose = verbose;
        this.IamProposing = new AtomicBoolean();
        this.WaitingForMasterAnswer = new AtomicBoolean();
        // create UDP socket
        this.serverSocket = new DatagramSocket(
                Config.getInstance().getThisServer().getBuddyPort(),
                Config.getInstance().getThisServer().getAddress());
        // initialize the executor, it is useful to execute methods as thread
        this.executor = Executors.newCachedThreadPool();
    }

    /**
     * this thread update datas about master server
     */
    private class updateMaster implements Runnable {

        private final InetAddress remoteAddr;
        private final int remotePort;

        /**
         *
         * @param remoteAddr the new master inet addr
         * @param remotePort the new master buddy port
         */
        public updateMaster(InetAddress remoteAddr, int remotePort) {
            this.remoteAddr = remoteAddr;
            this.remotePort = remotePort;
        }

        @Override
        public void run() {
            Config.getInstance().setMaster(remoteAddr, remotePort);
        }
    }

    /**
     * this thread send back a message
     */
    private class answer implements Runnable {

        private final InetAddress remoteAddr;
        private final int remotePort;
        private String data;

        /**
         *
         * @param data the data to send
         * @param addr the remote inet addr
         * @param port the remote port
         */
        public answer(String data, InetAddress addr, int port) {
            this.data = data;
            this.remoteAddr = addr;
            this.remotePort = port;
        }

        @Override
        public void run() {
            try {
                send(this.data, this.remoteAddr, this.remotePort);
            } catch (IOException ex) {
                ex.printStackTrace(System.out);
            }
        }
    }

    /**
     * this thread checks master liveness
     */
    private class checkMaster implements Runnable {

        private final int checkEvery; // time in second between 2 check
        private final int consideredDeadAfter; // time in second

        public checkMaster() {
            checkEvery = 20;
            consideredDeadAfter = 5;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    sleep(this.checkEvery * 1000);
                    if (!Config.getInstance().IamTheMaster()) {
                        try {
                            sendMaster("UP\n");
                        } catch (IOException ex) {
                            ex.printStackTrace(System.out);
                        }
                        WaitingForMasterAnswer.set(true);
                        sleep(this.consideredDeadAfter * 1000);
                        if (WaitingForMasterAnswer.get()) {
                            executor.submit(new propose());
                        }
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace(System.out);
                }
            }
        }
    }

    /**
     * this thread launches a vote
     */
    private class propose implements Runnable {

        public propose() {
        }

        public void run() {
            if (!IamProposing.get()) {
                try {
                    IamProposing.set(true);
                    sendHigher("ELECTION\n");
                    sleep(10 * 1000);
                    // if no OK received before 10 seconds last
                    // then send coordinator
                    if (IamProposing.get()) {
                        IamProposing.set(false);
                        sendAll("COORDINATOR\n");
                        Config.getInstance().setMaster();
                    }
                } catch (InterruptedException | IOException ex) {
                    ex.printStackTrace(System.out);
                }
            }
        }
    }

    /**
     * this thread allows connection testing
     */
    private class sayHello implements Runnable {

        public sayHello() {
        }

        @Override
        public void run() {
            try {
                sendAll("HELLO\n");
            } catch (IOException ex) {
                ex.printStackTrace(System.out);
            }
        }
    }

    /**
     * this thread print the server status every second
     */
    private class statusWorker implements Runnable {

        int num;
        int count;

        public statusWorker(int num) {
            this.num = num;
            this.count = 0;
        }

        @Override
        public void run() {
            while (true) {
                if (verbose) System.out.println("work" + this.num + " " + this.count + "s");
                if (verbose) System.out.println("status : "
                        + Config.getInstance().getThisServer().getState().
                        toString());
                if (!Config.getInstance().IamTheMaster()) {
                    ServerData master = Config.getInstance().getMaster();
                    if (master != null){
                        int bp = master.getBuddyPort();
                        if (verbose) System.out.println("master is : " + bp);
                    }
                }
                try {
                    sleep(1000); // sleep for one second
                    this.count++;
                } catch (InterruptedException ex) {
                    ex.printStackTrace(System.out);
                }
            }
        }
    }

    @Override
    public void run() {
        executor.submit(new statusWorker(1));
        executor.submit(new propose());
        executor.submit(new checkMaster());
        // main loop
        byte[] receiveData = new byte[1024];
        while (true) {
            try {
                // listen port
                DatagramPacket receivePacket
                        = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String msg = new String(receivePacket.getData());
                InetAddress remoteAddr = receivePacket.getAddress();
                int remotePort = receivePacket.getPort();
                if (verbose) System.out.println("RECEIVED FROM " + remoteAddr + ":" + remotePort
                        + ": " + msg);

                // analyse received message
                if (msg.startsWith("ELECTION")) {
                    executor.submit(new answer("OK\n", remoteAddr,
                            remotePort));
                    if (!Config.getInstance().IamTheMaster()){
                        executor.submit(new propose());
                    }
                } else if (msg.startsWith("COORDINATOR")) {
                    this.IamProposing.set(false);
                    this.WaitingForMasterAnswer.set(false);
                    executor.submit(new updateMaster(remoteAddr,
                            remotePort));
                } else if (msg.startsWith("OK") && this.IamProposing.get()) {
                    this.IamProposing.set(false);
                } else if (msg.startsWith("UP")
                        && Config.getInstance().IamTheMaster()) {
                    executor.submit(new answer("UP\n", remoteAddr,
                            remotePort));
                } else if (msg.startsWith("UP")
                        && this.WaitingForMasterAnswer.get()) {
                    this.WaitingForMasterAnswer.set(false);
                }

            } catch (IOException ex) {
                ex.printStackTrace(System.out);
            } finally {
                serverSocket.disconnect();
            }
        }
    }

    /**
     * send a message to the specified port
     *
     * @param data
     * @param addr
     * @param port
     * @throws IOException
     */
    private void send(String data, InetAddress addr, int port)
            throws IOException {
        byte[] sendData = data.getBytes();
        DatagramPacket sendPacket
                = new DatagramPacket(sendData, sendData.length, addr, port);
        this.serverSocket.send(sendPacket);
        if (verbose) System.out.println("SEND TO " + addr + ":" + port
                + ": " + data);
    }

    /**
     * send a message to the master
     *
     * @param data
     * @throws IOException
     */
    private void sendMaster(String data) throws IOException {
        // go through the IP, if higher send data
        ServerData master = Config.getInstance().getMaster();
        this.send(data, master.getAddress(), master.getBuddyPort());
    }

    /**
     * send a message to all the server which have a higher score than this
     * server
     *
     * @param data
     * @throws IOException
     */
    private void sendHigher(String data) throws IOException {

        HashMap<Integer, ServerData> list;
        list = Config.getInstance().getServerList();
        Iterator<Integer> itr;
        itr = list.keySet().iterator();

        // go through the IP, if higher send data
        while (itr.hasNext()) {
            Integer key = itr.next();
            ServerData value;
            value = list.get(key);
            //TOFIX : compare ip addr
            if (value.getBuddyPort()
                    > Config.getInstance().getThisServer().getBuddyPort()) {
                this.send(data, value.getAddress(), value.getBuddyPort());
            }
        }
    }

    /**
     * send a message to every recorded servers
     *
     * @param data
     * @throws IOException
     */
    private void sendAll(String data) throws IOException {

        HashMap<Integer, ServerData> list;
        list = Config.getInstance().getServerList();
        Iterator<Integer> itr;
        itr = list.keySet().iterator();

        // go through the ips, if different than mine send data
        while (itr.hasNext()) {
            Integer key = itr.next();
            ServerData value;
            value = list.get(key);
            this.send(data, value.getAddress(), value.getBuddyPort());
        }
    }
}
