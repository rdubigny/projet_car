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
public class NameNode extends Thread {
    private final boolean verbose;

    public NameNode(boolean verbose) {
        this.verbose = verbose;
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
        return 0;
    }
    
}
