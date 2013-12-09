/*
 * This is an utility tools that feel up the config structure by reading the 
 * config file.
 */
package client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

class ServerProperty {

    /* attributes */
    private int idServer;
    private InetAddress ipAddress;
    private int clientPort;

    /* constructor */
    public ServerProperty(int idServer, InetAddress ipAddress,
            int clientPort) {
        this.idServer = idServer;
        this.ipAddress = ipAddress;
        this.clientPort = clientPort;
    }

    /* getter, setter*/
    /**
     * @return the idServer
     */
    public int getIdServer() {
        return idServer;
    }

    /**
     * @param idServer the idServer to set
     */
    public void setIdServer(int idServer) {
        this.idServer = idServer;
    }

    /**
     * @return the ipAddress
     */
    public InetAddress getIpAddress() {
        return ipAddress;
    }

    /**
     * @param ipAddress the ipAddress to set
     */
    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * @return the clientPort
     */
    public int getClientPort() {
        return clientPort;
    }

    /**
     * @param clientPort the clientPort to set
     */
    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

}

public class Config {
    
    public static ArrayList<ServerProperty> config;
    
    public static void readConfigFile() {
        config = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("config.txt"));
            String line;
            while (((line = br.readLine()) != null) && !line.equals("")) {
                // get data of one line
                String[] tokens = line.split(" ");
                int idServer = Integer.valueOf(tokens[0]);
                InetAddress ipAddress = InetAddress.getByName(tokens[1]);
                int clientPort = Integer.parseInt(tokens[2]);
                // store data of this line
                ServerProperty serverProperty = 
                        new ServerProperty(idServer, ipAddress, clientPort);
                config.add(serverProperty);
            }
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace(System.out);             
        }
    }
}
