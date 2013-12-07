/*
 * This is an utility tools that feel up the config structure by reading the 
 * config file.
 */
package server.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

/**
 *
 * @author car team 16
 */
public class ConfigReader {

    public static synchronized void readConfigFile(int me) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("config.txt"));
            String line;
            while (((line = br.readLine()) != null) && !line.equals("")) {
                // format line datas
                String[] tokens = null;
                tokens = line.split(" ");
                int id = Integer.valueOf(tokens[0]);
                // generate localhost inet address
                InetAddress tmpAddr;
                tmpAddr = InetAddress.getByName(tokens[1]);
                
                // format buddy port
                int buddyPort = Integer.parseInt(tokens[2]);
                int clientPort = Integer.parseInt(tokens[3]);
                int serverPort = Integer.parseInt(tokens[4]);
            
                // fill in the config structure
                if (id != me) {
                    // fill in the Config serverList
                    Config.getInstance().addData(id, tmpAddr, buddyPort, 
                            clientPort, serverPort);
                }else{
                    // fill in the config info for this server
                    Config.getInstance().thisServer(id, tmpAddr, buddyPort, 
                            clientPort, serverPort);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        } finally {
            try{
                br.close();
            } catch (IOException ex) {
                ex.printStackTrace(System.out);
            }
        }
    }
}
