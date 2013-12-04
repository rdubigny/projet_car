/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

/**
 *
 * @author car team 16
 */
public class NameNodeManager extends Thread {
    private final boolean verbose;

    /**
     *
     * @param verbose
     */
    public NameNodeManager(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (this.verbose)
                    System.out.println("NameNodeManager: I'm alive");
                BuddyManager.sleep(2000);
            } catch (InterruptedException ex) {
                ex.printStackTrace(System.out);
            }
        }
    }
    
}
