/*
 * ServerData is a data structure that contain all the information needed to 
 * describe a server.
 */
package server.utils;

import java.net.InetAddress;

/**
 *
 * @author car team 16
 */
public class ServerData {

    private int id;

    private InetAddress address; // IP address
    private int buddyPort; // Port used for Buddy algorithm
    private int clientPort; // port used to listen to clients
    private int serverPort; //port used to listen to other servers
    private State state;
    
    /**
     * 
     * @param id
     * @param address
     * @param buddyPort
     * @param clientPort
     * @param serverPort
     * @param state 
     */
    public ServerData(int id, InetAddress address, int buddyPort, 
            int clientPort, int serverPort, State state) {
        this.id = id;
        this.address = address;
        this.buddyPort = buddyPort;
        this.clientPort = clientPort;
        this.serverPort = serverPort;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    /**
     * @return the address
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * @return the buddyPort
     */
    public int getBuddyPort() {
        return buddyPort;
    }

    /**
     * @return the clientPort
     */
    public int getClientPort() {
        return clientPort;
    }

    /**
     * @return the serverPort
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * @return the state
     */
    public State getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(State state) {
        this.state = state;
    }

}
