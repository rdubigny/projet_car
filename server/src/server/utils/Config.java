/*
 * Config is a static contener which keep in memory the informations about the 
 * state of the different servers.
 */
package server.utils;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author car team 16
 */
public class Config {

    private final HashMap<Integer, ServerData> serverList;
    private ServerData thisServer;
    private int masterId;

    // As this is static, the VM makes sure that this is called only once
    private static final Config INSTANCE = new Config();

    // We know the contstructor is called only once.  Making it private
    // guarentees no other classes can call it.
    // ==> Thus this is a nice safe place to initialize your Hash
    public Config() {
        serverList = new HashMap<>();
        thisServer = new ServerData();
        masterId = -1; // means no master yet
    }

    public static synchronized Config getInstance() {
        return INSTANCE;
    }

    public void addData(int id, InetAddress address, int buddyPort) {
        ServerData data = new ServerData(id, address, buddyPort, State.DATA);
        serverList.put(id, data);
    }

    public void thisServer(int id, InetAddress address, int buddyPort) {
        this.thisServer
                = new ServerData(id, address, buddyPort, State.DATA);
    }

    public ServerData getThisServer() {
        return thisServer;
    }

    public void setPortEmissionMaster(int id, String string) {
        serverList.get(id).setPortEmissionMaster(string);
    }

    /**
     * mostly used to learn remote master
     *
     * @param address
     * @param buddyPort
     */
    public void setMaster(InetAddress address, int buddyPort) {

        Iterator<Integer> itr = this.serverList.keySet().iterator();
        this.masterId = -1;
        this.thisServer.setState(State.DATA);

        while (itr.hasNext()) {
            Integer key = itr.next();
            ServerData data = this.serverList.get(key);
            if (data.getState() == State.MASTER) {
                data.setState(State.DATA);
            }
            // if (data.getAddress().equals(address)// TOFIX this don't
            if (data.getBuddyPort() == buddyPort) {
                data.setState(State.MASTER);
                this.masterId = key;
            }
            // if this config wasn't found in the list, it's this one perhaps
            if (this.masterId == -1) {
                if (this.thisServer.getAddress().equals(address)
                        && this.thisServer.getBuddyPort() == buddyPort) {
                    this.thisServer.setState(State.MASTER);
                    this.masterId = this.thisServer.getId();
                }
            }
        }
    }

    /**
     * set this server as master
     */
    public void setMaster() {
        if (!this.IamTheMaster()) {
            if (this.masterId != -1) {
                this.serverList.get(this.masterId).setState(State.DATA);
            }
            this.thisServer.setState(State.MASTER);
            this.masterId = this.thisServer.getId();
        }
    }

    public ServerData getMaster() {
        if (this.masterId == -1) {
            return null;
        } else if (this.thisServer.getId() == this.masterId) {
            return this.thisServer;
        } else {
            return this.serverList.get(masterId);
        }
    }

    public boolean IamTheMaster() {
        return (this.thisServer.getState() == State.MASTER);
    }

    public HashMap<Integer, ServerData> getServerList() {
        return this.serverList;
    }
}
