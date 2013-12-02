/*
 * Used to store datas about servers
 */

package server;

import java.net.InetAddress;

/**
 *
 * @author scar
 */
public class Address {

    public InetAddress address; // IP address
    public int buddyPort; // Port used for Buddy algorithm
    public State state;

    public Address(InetAddress address, int port, State state) {
        this.address = address;
        this.buddyPort = port;
        this.state = state;
    }
}
