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

import memory.*;

public class Client {
    private Messenger messenger;
    
    private static final Scanner scanner = new Scanner(System.in);
    
    public Client(String host, int port) {
        try {
            messenger = new Messenger(new Socket(host, port));
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // FIXME: this attributes should belong to Client class
        
        /*Owner client = new Owner(1, login, 3, "192.168.1.3");
        System.out.println(client.id);
        // Initialization of the memory
        //HashMap memory = new HashMap();
        Memory memory = new Memory();   
        memory.insert(client); 
        memory.display();*/
        while (true)
            run("127.0.0.1", 10300);
    }
    
    private static void run(String host, int port) {
        System.out.println("Welcome to your shared file system!");
        Client user = new Client(host, port);
        user.login();
        DataContainer command;
        boolean connexionHasFinished;
        do {
            System.out.print("> ");
            command = EntryFormatter.format(scanner);
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
    
    private void login() {
        System.out.print("Please type your login : ");
        String login = scanner.nextLine();
        DataContainer request = new DataContainer("CONNECT", login);
        execute(request);
    }
    
    private void close() {
        messenger.close();
    }
}
