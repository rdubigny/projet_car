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

    public NameNode() {
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("NameNode: I'm alive");
                BuddyManager.sleep(3000);
            } catch (InterruptedException ex) {
            }
        }
    }
    
}
