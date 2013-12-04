/**
 *
 * @author gyls
 */
package client;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import request.*;
import memory.*;

public class Client {
    private Messenger messenger;
    private Socket serverSocket;
    
    private static final Scanner scanner = new Scanner(System.in);
    
    public Client(String host, int port) {
        try {
            serverSocket = new Socket(host, port);
            messenger = new Messenger(serverSocket);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Message of welcome
        System.out.println("Bienvenue sur votre système de fichiers partagés' !");
        // Message of connexion
        System.out.print("Entrer votre login : ");
        String login = scanner.nextLine();
        
        
        
        // FIXME: this attributes should belong to Client class
        
        // Suppose that the client exists
        Owner client = new Owner(1, login, 3, "192.168.1.3");
        System.out.println(client.id);
        // Initialization of the memory
        //HashMap memory = new HashMap();
        Memory memory = new Memory();   
        memory.insert(client); 
        memory.display();
        
        startConnexion("127.0.0.1", 10300);
    }
    
    private static void startConnexion(String host, int port) {
        Client user = new Client(host, port);
        user.execute("CONNECT");
        String command;
        do {
            System.out.print("> ");
            command = scanner.nextLine().trim().toUpperCase();
            user.execute(command);
        } while (! command.startsWith("QUIT"));
    }
    
    private void execute(String command) {
        messenger.send(command);
        System.out.println(messenger.receivedMessage());
    }
}
