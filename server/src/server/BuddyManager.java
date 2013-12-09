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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import server.utils.Config;
import server.utils.ServerData;
import server.utils.Status;

/**
 *
 * @author car team 16
 */
public class BuddyManager extends Thread {

    // class constants
    private final int checkEvery = 10; // time in second between 2 up check
    private final int consideredDeadAfter = 1; // time in second
    private final int electionTimeout = 2;

    private final DatagramSocket serverSocket; // UDP socket

    // private final MulticastSocket multiServerSocket; // UDP multicast socket
    private AtomicBoolean IamProposing;
    private AtomicBoolean WaitingForMasterAnswer;
    private ConcurrentHashMap<Integer, Status> statusTable;
    private final Object lockStatusTable;
    private final ExecutorService executor;

    private final boolean verbose;

    /**
     * Initialize socket
     *
     * @param verbose
     * @throws IOException
     */
    public BuddyManager(boolean verbose) throws IOException {
        this.lockStatusTable = new Object();
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
    private class checkStatus implements Runnable {

        public checkStatus() {
            statusTable = new ConcurrentHashMap<>();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    sleep(checkEvery * 1000);
                    if (!Config.getInstance().IamTheMaster()) {
                        // check master liveness
                        WaitingForMasterAnswer.set(true);
                        try {
                            sendMaster("MASTERUP");
                        } catch (IOException ex) {
                            ex.printStackTrace(System.out);
                        }
                        sleep(consideredDeadAfter * 1000);
                        if (WaitingForMasterAnswer.get()) {
                            executor.submit(new propose());
                        }
                    } else if (Config.getInstance().ThereIsAMaster()) {
                        // check all servers liveness
                        Set<Integer> keySet = Config.getInstance().getServerList().keySet();
                        Iterator<Integer> keyItr = keySet.iterator();
                        // we must ensure that propose is not modifying
                        // statusTable in the same time
                        synchronized(lockStatusTable){                       
                            while (keyItr.hasNext()) {
                                int key = keyItr.next();
                                statusTable.put(key, Status.DOWN);
                            }
                            try {
                                sendAll("UP");
                            } catch (IOException ex) {
                                ex.printStackTrace(System.out);
                            }
                            sleep(consideredDeadAfter * 1000);
                            int SecDown = 0;
                            Iterator<Integer> itr;
                            itr = statusTable.keySet().iterator();

                            // record the down servers if necessary
                            while (itr.hasNext()) {
                                Integer key = itr.next();
                                Status newSt, oldSt;
                                newSt = statusTable.get(key);
                                oldSt = Config.getInstance().getServerList().get(key).getStatus();
                                //System.out.println(key+" is "+newSt+" and was "+oldSt);
                                if (newSt == Status.DOWN && oldSt != newSt) {
                                    if (verbose) {
                                        System.out.println("server" + key + " went DOWN!");
                                    }
                                    if (oldSt == Status.SECONDARY) {
                                        SecDown++;
                                    }
                                    Config.getInstance().getServerList().
                                            get(key).setStatus(Status.DOWN);
                                }
                            }
                            if (SecDown > 0) {
                                Server.nameNodeManager.electNewSecondary(SecDown);
                            }
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

        @Override
        public void run() {
            if (!IamProposing.get()) {
                try {
                    IamProposing.set(true);
                    sendHigher("ELECTION");
                    sleep(electionTimeout * 1000);
                    // if no OK received before n seconds last
                    // then send coordinator
                    if (IamProposing.get()) {
                        IamProposing.set(false);
                        sendAll("COORDINATOR");
                        Config.getInstance().setMaster();
                        // now we need to learn the status
                        // generate a new list of status
                        Set<Integer> keySet = Config.getInstance().getServerList().keySet();
                        Iterator<Integer> keyItr = keySet.iterator();
                        // we must ensure that checkStatus is not modifying
                        // statusTable in the same time
                        synchronized(lockStatusTable){
                            // mark all status as down
                            while (keyItr.hasNext()) {
                                int key = keyItr.next();
                                statusTable.put(key, Status.DOWN);
                            }
                            // broadcast "up" to all so they send back their status
                            try {
                                sendAll("UP");
                            } catch (IOException ex) {
                                ex.printStackTrace(System.out);
                            }
                            try {
                                sleep(consideredDeadAfter * 1000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace(System.out);
                            }
                            // update all status
                            Iterator<Integer> itr;
                            itr = statusTable.keySet().iterator();

                            while (itr.hasNext()) {
                                Integer key = itr.next();
                                Status newSt = statusTable.get(key);
                                Config.getInstance().getServerList().
                                        get(key).setStatus(newSt);
                            }
                        }
                    }
                } catch (InterruptedException | IOException ex) {
                    ex.printStackTrace(System.out);
                }
            }
        }
    }

    /**
     * this thread implement an improvement of the buddy algorithm. It is called
     * at boot. It broadcast an hello message that is answered only by the
     * master if there is one in the network, otherwise it launch an election.
     */
    private class sayHello implements Runnable {

        public sayHello() {
        }

        @Override
        public void run() {
            try {
                WaitingForMasterAnswer.set(true);
                try {
                    sendAll("HELLO " + Config.getInstance().getThisServer().getId());
                } catch (IOException ex) {
                    ex.printStackTrace(System.out);
                }
                sleep(consideredDeadAfter * 1000);
                if (WaitingForMasterAnswer.get()) {
                    executor.submit(new propose());
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace(System.out);
            }

        }
    }

    /**
     * this thread print the server status every second
     */
    private class statusWorker implements Runnable {

        int id;
        int count;

        public statusWorker() {
            this.id = Config.getInstance().getThisServer().getId();
            this.count = 0;
        }

        @Override
        public void run() {
            while (true) {
                System.out.println("server" + this.id + ": " + this.count + "top");
                System.out.println("status: "
                        + Config.getInstance().getThisServer().getStatus().
                        toString());
                if (!Config.getInstance().IamTheMaster()) {
                    ServerData master = Config.getInstance().getMaster();
                    if (master != null) {
                        int bp = master.getBuddyPort();
                        System.out.println("master is : " + bp);
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
        if (verbose) {
            executor.submit(new statusWorker());
        }
        executor.submit(new sayHello());
        executor.submit(new checkStatus());
        // main loop : listen the buddy port and launch appropriate threads to 
        // answer
        while (true) {
            try {
                byte[] receiveData = new byte[100];
                // listen port
                DatagramPacket receivePacket
                        = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String msg = new String(receivePacket.getData());
                InetAddress remoteAddr = receivePacket.getAddress();
                int remotePort = receivePacket.getPort();
                if (verbose) {
                    System.out.println("RECEIVED FROM " + remoteAddr + ":" + remotePort
                            + ": " + msg);
                }

                // analyse received message
                if (msg.startsWith("ELECTION")) {
                    executor.submit(new answer("OK", remoteAddr,
                            remotePort));
                    if (!Config.getInstance().IamTheMaster()) {
                        executor.submit(new propose());
                    }
                } else if (msg.startsWith("COORDINATOR")) {
                    this.IamProposing.set(false);
                    this.WaitingForMasterAnswer.set(false);
                    executor.submit(new updateMaster(remoteAddr,
                            remotePort));
                } else if (msg.startsWith("OK") && this.IamProposing.get()) {
                    this.IamProposing.set(false);
                } else if (msg.startsWith("MASTERUP")
                        && Config.getInstance().IamTheMaster()) {
                    executor.submit(new answer("MASTERUP", remoteAddr,
                            remotePort));
                } else if (msg.startsWith("MASTERUP")
                        && this.WaitingForMasterAnswer.get()) {
                    this.WaitingForMasterAnswer.set(false);
                } else if (msg.startsWith("HELLO")
                        && Config.getInstance().IamTheMaster()) {
                    executor.submit(new answer("COORDINATOR", remoteAddr,
                            remotePort));
                    int id = Integer.parseInt(msg.substring(6).trim());
                    Config.getInstance().getServerList().get(id).setStatus(
                            Status.DATA);
                    // if the server have just connected and missed the UP 
                    // message then he will be considered dead
                    // we must enter a value in statusTable for this case
                    statusTable.put(id, Status.DATA);
                } else if (msg.startsWith("UP")
                        && !Config.getInstance().IamTheMaster()) {
                    String resp = "UP "
                            + Config.getInstance().getThisServer().getId() + " "
                            + Config.getInstance().getThisServer().getStatus().toString();
                    executor.submit(new answer(resp, remoteAddr,
                            remotePort));
                } else if (msg.startsWith("UP")
                        && Config.getInstance().IamTheMaster()) {
                    String[] infos = msg.split(" ");
                    int fId = Integer.parseInt(infos[1]);
                    Status fNewState = Status.valueOf(infos[2].trim());
                    statusTable.put(fId, fNewState);
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
        String concat = data.concat("\0");
        byte[] sendData = concat.getBytes();
        DatagramPacket sendPacket
                = new DatagramPacket(sendData, sendData.length, addr, port);
        this.serverSocket.send(sendPacket);
        if (verbose) {
            System.out.println("SEND TO " + addr + ":" + port
                    + ": " + data);
        }
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
