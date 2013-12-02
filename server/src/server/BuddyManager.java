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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

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

    /**
     * Initialize socket
     *
     * @throws IOException
     */
    public BuddyManager() throws IOException {
        this.IamProposing = new AtomicBoolean();
        this.WaitingForMasterAnswer = new AtomicBoolean();
        // create UDP socket
        this.serverSocket = new DatagramSocket(Server.myAddress.buddyPort,
                Server.myAddress.address);
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
            if (Server.myAddress.state == server.State.MASTER) {
                Server.myAddress.state = server.State.DATA;
            }
            for (Address itr : Server.adresses) {
                if (itr.state == server.State.MASTER) {
                    itr.state = server.State.DATA;
                }
                //if (itr.address == remoteAddr && //TOFIX : can't compare addr
                if (itr.buddyPort == remotePort) {
                    itr.state = server.State.MASTER;
                }
            }
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
                    if (Server.myAddress.state != server.State.MASTER){
                        try {
                            sendMaster("UP\n");
                        } catch (IOException ex) {
                        }
                        WaitingForMasterAnswer.set(true);
                        sleep(this.consideredDeadAfter*1000);
                        if (WaitingForMasterAnswer.get()){
                            executor.submit(new propose());
                        }
                    }
                } catch (InterruptedException ex) {
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
                        Server.myAddress.state = server.State.MASTER;
                        for (Address itr : Server.adresses) {
                            if (itr.state == server.State.MASTER) {
                                itr.state = server.State.DATA;
                            }
                        }
                    }
                } catch (InterruptedException | IOException ex) {
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
                System.out.println("work" + this.num + " " + this.count + "s");
                System.out.println("status : "
                        + Server.myAddress.state.toString());
                for (Address itr : Server.adresses) {
                    if (itr.state.compareTo(server.State.MASTER) == 0) {
                        System.out.println("master is : " + itr.buddyPort);
                    }
                }
                try {
                    sleep(1000);
                    this.count++;
                } catch (InterruptedException ex) {
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
                System.out.println("RECEIVED FROM " + remoteAddr + ":" + remotePort
                        + ": " + msg);
                
                // analyse received message
                if (msg.startsWith("ELECTION")) {
                    executor.submit(new answer("OK\n", remoteAddr,
                            remotePort));
                    executor.submit(new propose());
                } else if (msg.startsWith("COORDINATOR")) {
                    this.IamProposing.set(false);
                    this.WaitingForMasterAnswer.set(false);
                    executor.submit(new updateMaster(remoteAddr,
                            remotePort));
                } else if (msg.startsWith("OK") && this.IamProposing.get()) {
                    this.IamProposing.set(false);
                } else if (msg.startsWith("UP") 
                        && Server.myAddress.state == server.State.MASTER){
                    executor.submit(new answer("UP\n", remoteAddr,
                            remotePort));
                } else if (msg.startsWith("UP") 
                        && this.WaitingForMasterAnswer.get()){
                    this.WaitingForMasterAnswer.set(false);
                }

            } catch (IOException ex) {
                System.out.println(ex.getStackTrace());
            } finally {
                serverSocket.disconnect();
            }
        }
    }

    /**
     * send a message to the specified port
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
        System.out.println("SEND TO " + addr + ":" + port
                + ": " + data);
    }

    /**
     * send a message to the master
     * @param data
     * @throws IOException 
     */
    private void sendMaster(String data) throws IOException {
        // go through the IP, if higher send data
        for (Address itr : Server.adresses) {
            if (itr.state == server.State.MASTER) {
                this.send(data, itr.address, itr.buddyPort);
            }
        }
    }

    /**
     * send a message to all the server which have a higher score than this server
     * @param data
     * @throws IOException 
     */
    private void sendHigher(String data) throws IOException {
        // go through the IP, if higher send data
        for (Address itr : Server.adresses) {
            if (itr.buddyPort > Server.myAddress.buddyPort) { //TOFIX : compare ip addr
                this.send(data, itr.address, itr.buddyPort);
            }
        }
    }

    /**
     * send a message to every recorded servers
     * @param data
     * @throws IOException 
     */
    private void sendAll(String data) throws IOException {
        // go through the ips, if different than mine send data
        for (Address itr : Server.adresses) {
            this.send(data, itr.address, itr.buddyPort);
        }
    }
}
