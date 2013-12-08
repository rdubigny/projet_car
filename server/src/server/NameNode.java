/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import java.util.HashMap;
import java.util.List;
import server.utils.Config;
import server.utils.Status;

/**
 *
 * @author car team 16
 */
public class NameNode extends Thread {
    private final boolean verbose;
    private HashMap<String, List> theNode; 

    public NameNode(boolean verbose) {
        this.verbose = verbose;
        theNode = new HashMap<>();
    }

    @Override
    public void run() {
        while (true) {
            synchronized (Config.getInstance().lockStatus) {
                try {
                    Config.getInstance().lockStatus.wait();
                    if (Config.getInstance().getStatut() == Status.SECONDARY){
                        if (verbose) System.out.println("NameNode activated!");
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace(System.out);
                }
            }
        }
    }
    
    /**
     * 
     * @return zero if the process finished successfully
     */
    public int update() {
        System.out.println("nameNode.update");
        // TODO : update NameNode
        return 0;
    }
    
}
