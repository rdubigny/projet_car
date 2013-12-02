/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package server.utils;

import java.net.InetAddress;
import server.State;

/**
 *
 * @author scar
 */
public class ServerData {

    private InetAddress address; // IP address
    private int buddyPort; // Port used for Buddy algorithm
    private State state;
    
    private int Grade;
    private String IpMaster;
    private String Master;
    private String PortEmissionMaster;
    private String PortReceptionServer;
    private String PortEmissionServer;
    private String PortReceptionClient;

    // We know the contstructor is called only once.  Making it private
    // guarentees no other classes can call it.
    // ==> Thus this is a nice safe place to initialize your Hash
    public ServerData(InetAddress address, int buddyPort, State state) {
        this.address = address;
        this.buddyPort = buddyPort;
        this.state = state;
    }  
    
    public ServerData() {
        Master = null;
        Grade = 0;        
    }

    /**
     * @return the address
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(InetAddress address) {
        this.address = address;
    }

    /**
     * @return the buddyPort
     */
    public int getBuddyPort() {
        return buddyPort;
    }

    /**
     * @param buddyPort the buddyPort to set
     */
    public void setBuddyPort(int buddyPort) {
        this.buddyPort = buddyPort;
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

    /**
     * @return the Grade
     */
    public int getGrade() {
        return Grade;
    }

    /**
     * @param Grade the Grade to set
     */
    public void setGrade(int Grade) {
        this.Grade = Grade;
    }

    /**
     * @return the IpMaster
     */
    public String getIpMaster() {
        return IpMaster;
    }

    /**
     * @param IpMaster the IpMaster to set
     */
    public void setIpMaster(String IpMaster) {
        this.IpMaster = IpMaster;
    }

    /**
     * @return the Master
     */
    public String getMaster() {
        return Master;
    }

    /**
     * @param Master the Master to set
     */
    public void setMaster(String Master) {
        this.Master = Master;
    }

    /**
     * @return the PortEmissionMaster
     */
    public String getPortEmissionMaster() {
        return PortEmissionMaster;
    }

    /**
     * @param PortEmissionMaster the PortEmissionMaster to set
     */
    public void setPortEmissionMaster(String PortEmissionMaster) {
        this.PortEmissionMaster = PortEmissionMaster;
    }

    /**
     * @return the PortReceptionServer
     */
    public String getPortReceptionServer() {
        return PortReceptionServer;
    }

    /**
     * @param PortReceptionServer the PortReceptionServer to set
     */
    public void setPortReceptionServer(String PortReceptionServer) {
        this.PortReceptionServer = PortReceptionServer;
    }

    /**
     * @return the PortEmissionServer
     */
    public String getPortEmissionServer() {
        return PortEmissionServer;
    }

    /**
     * @param PortEmissionServer the PortEmissionServer to set
     */
    public void setPortEmissionServer(String PortEmissionServer) {
        this.PortEmissionServer = PortEmissionServer;
    }

    /**
     * @return the PortReceptionClient
     */
    public String getPortReceptionClient() {
        return PortReceptionClient;
    }

    /**
     * @param PortReceptionClient the PortReceptionClient to set
     */
    public void setPortReceptionClient(String PortReceptionClient) {
        this.PortReceptionClient = PortReceptionClient;
    }
    
}
