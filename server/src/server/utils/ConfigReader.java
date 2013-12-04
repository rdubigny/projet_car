/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

/**
 *
 * @author scar
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
            
                // fill in the config structure
                if (id != me) {
                    // fill in the Config serverList
                    Config.getInstance().addData(id, tmpAddr, buddyPort);
                    
                    Config.getInstance().setPortEmissionMaster(id, tokens[3]);
                    // TO COMPLETE
                }else{
                    // fill in the config info for this server
                    Config.getInstance().thisServer(id, tmpAddr, buddyPort);
                    Config.getInstance().getThisServer().
                            setPortEmissionMaster(tokens[2]);
                    // TO COMPLETE
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getStackTrace());
        } finally {
            try{
                br.close();
            } catch (IOException ex) {
                System.out.println(ex.getStackTrace());
            }
        }
    }
}
