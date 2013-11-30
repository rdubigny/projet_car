/*
 * BuddyManager implement the buddy algorithm for this server. It also maintain 
 * a list of the servers and their respective roles.
 */
package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *
 * @author car team 16
 */
public class BuddyManager extends Thread {

    private final int portNumber = 4444;
    private final InetAddress localAddr = null; // resolve into localhost;
    private final DatagramSocket serverSocket; // UDP socket
    
    public BuddyManager() throws IOException {
        // create socket
        serverSocket = new DatagramSocket(portNumber, localAddr);
    }

    @Override
    public void run() {
        // main loop
        byte[] sendData;
        // can receive 'Election', 'OK' or 'Coordinator'
        byte[] receiveData = new byte[1024];
        while(true){
            try {
                DatagramPacket receivePacket =
                        new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String sentence = new String( receivePacket.getData());
                System.out.println("RECEIVED: " + sentence);                
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();
                String capitalizedSentence = sentence.toUpperCase();
                sendData = capitalizedSentence.getBytes();
                DatagramPacket sendPacket =
                        new DatagramPacket(sendData, sendData.length, IPAddress, port);
                serverSocket.send(sendPacket);
            } catch (IOException ex) {
                System.out.println(ex.getStackTrace());
            } finally {
                serverSocket.disconnect();
            }
        }
    }
}

// TCP connexion example
/*
private final ServerSocket serverSocket;
private final int backlog = 50; // number max of simultaneous connections
serverSocket = new ServerSocket(portNumber, backlog, inetAddr);
while (false) {
    Socket clientSocket = null;
    try {
        clientSocket = serverSocket.accept();
        System.out.println("BuddyManager: connexion received, "
                +"opening a new connexion on port "
                +clientSocket.getLocalPort());
        PrintWriter clientOut =
            new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader clientIn = new BufferedReader(
            new InputStreamReader(clientSocket.getInputStream()));
        clientOut.println("yo! welcome to this f*****g java server!");
        String tmp;
        while((tmp = clientIn.readLine()) != null){
            System.out.println(tmp);
        }
    } catch (IOException ex) {
        System.out.println(ex.getStackTrace());
    } finally {
        try {
            clientSocket.close();
        } catch (IOException ex) {
            System.out.println(ex.getStackTrace());
            Logger.getLogger(BuddyManager.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }
}
*/
