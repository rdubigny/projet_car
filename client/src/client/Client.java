package client;

import data.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Client {
    /* attribute */
    private Messenger messenger;
    private static final EntryFormatter entryFormatter = 
            new EntryFormatter(new Scanner(System.in));
    
    /* constructor */
    public Client(String host, int port) {
        try {
            messenger = new Messenger(new Socket(host, port));
        } catch (IOException e) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    /* method */
    private static void run(String host, int port) {
        System.out.println("\nWelcome to your shared file system!");
        DataContainer command = entryFormatter.getLogin();
        Client user = new Client(host, port);
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
    
    /* main */
    public static void main(String[] args) {
        while (true)
            run("127.0.0.1", 1025);

    }
}
