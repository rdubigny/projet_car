/**
 *
 * @author gyls
 */
package client;

import data.Messenger;
import data.DataContainer;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Client {
    private Messenger messenger;
    
    private static final EntryFormatter entryFormatter = 
            new EntryFormatter(new Scanner(System.in));
    
    public Client(String host, int port) {
        try {
            messenger = new Messenger(new Socket(host, port));
        } catch (IOException e) {
            System.out.println("erreur");
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    public static void main(String[] args) {
        while (true)
            run("127.0.0.1", 1025);
    }
    
    private static void run(String host, int port) {
        System.out.println("\nWelcome to your shared file system!");
        DataContainer command = entryFormatter.getLogin();
        Client user = new Client(host, port);
        System.exit(0);
        user.execute(command);
        boolean connexionHasFinished;
        do {
            System.out.print("> ");
            command = entryFormatter.format();
            connexionHasFinished = user.execute(command);
        } while (!connexionHasFinished);
        user.close();
    }
    
    private boolean execute(DataContainer command) {
        messenger.send(command);
        DataContainer received = messenger.receive();
        System.out.println(received);
        return received.getContent().equals("Connexion was finished.");
    }
    
    private void close() {
        messenger.close();
    }
}
