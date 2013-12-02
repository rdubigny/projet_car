/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.utils;

import java.net.InetAddress;
import java.util.ArrayList;
import server.State;

/**
 *
 * @author scar
 */
public class Config {

    private final ArrayList<ServerData> ServerList;
    private ServerData thisServer;

    // As this is static, the VM makes sure that this is called only once
    private static final Config INSTANCE = new Config();

    // We know the contstructor is called only once.  Making it private
    // guarentees no other classes can call it.
    // ==> Thus this is a nice safe place to initialize your Hash
    private Config() {
        ServerList = new ArrayList<ServerData>();
        thisServer = new ServerData();
    }

    public static synchronized Config getInstance() {
        return INSTANCE;
    }

    public void addData(InetAddress address, int buddyPort, State state) {
        ServerData data = new ServerData(address, buddyPort, state);
        ServerList.add(data);
    }

    public ServerData getThisServer() {
        return thisServer;
    }

    public void setThisServer(ServerData thisServer) {
        this.thisServer = thisServer;
    }
}
