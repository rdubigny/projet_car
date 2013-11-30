/*
 * BuddyManager implement the buddy algorithm for this server. It also maintain 
 * a list of the servers and their respective roles.
 */
package server;

/**
 *
 * @author car team 16
 */
public class BuddyManager extends Thread {

    public BuddyManager() {
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("BuddyManager: I'm alive");
                BuddyManager.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }
}
