/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package server.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author scar
 */
public class ConfigReader {
    public static synchronized void readConfigFile(int Me) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("config.txt"));
            String line;
            int i =0;
            while (((line = br.readLine()) != null) ) {
                /* TODO
                // process the line.
                String[] tokens = null;
                tokens = line.split(" ");
                int CurrentToken = Integer.valueOf(tokens[0]);
                //Configuration Master
                if(CurrentToken == 0){
                
                Config.getInstance().setIpMaster(tokens[1]);
                }
                
                //MaPropreConfiguration
                
                if(CurrentToken == Me){
                
                    SubordanyList.getInstance().setMaster(tokens[1]);
                    SubordanyList.getInstance().setPortEmissionMaster(tokens[2]);
                    SubordanyList.getInstance().setPortEmissionServer(tokens[3]);
                    SubordanyList.getInstance().setPortReceptionClient(tokens[4]);
                    SubordanyList.getInstance().setPortReceptionServer(tokens[5]);
                    
                    if(Me == 0){SubordanyList.getInstance().setGrade(2);}
                    else if (0<Me && Me<10){SubordanyList.getInstance().setGrade(1);}
                    else {SubordanyList.getInstance().setGrade(0);}
                }
                else{
                    
                    
                     SubordanyList.getInstance().addIP(tokens[5]);
                    
                }*/
                
            }
            br.close();
            // afficherConfig();
            
            
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConfigReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConfigReader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(ConfigReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
