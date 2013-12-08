/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import java.util.HashMap;
import java.util.List;

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
            try {
                if (verbose) System.out.println("NameNode: I'm alive");
                BuddyManager.sleep(3000);
            } catch (InterruptedException ex) {
                ex.printStackTrace(System.out);
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
