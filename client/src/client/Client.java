/**
 *
 * @author gyls
 */
package client;

import java.io.*;
import java.net.*;
import java.util.*;

import request.*;
import memory.*;

public class Client {
    private Messenger messenger;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Initialization of the memory
        //HashMap memory = new HashMap();
        //Memory memory = new Memory();
        // Message of welcome
        System.out.println("Bienvenue sur votre système de fichiers partagés' !");
        // Message of connexion
        System.out.print("Entrer votre login : ");
        Scanner scanner = new Scanner(System.in);
        String login = scanner.nextLine();
        // Suppose that the client exists
        Owner client;
        client = new Owner(1, login, 3, "192.168.1.3");
        System.out.println(client.id);
        //memory.insert(client); 
        //memory.display();
  
        Client user = new Client();
        user.connectToServer("127.0.0.1", 10300);
        user.endConnection();
    }
    
    private void connectToServer(String host, int port) {
         try {
            Socket serverSocket = new Socket(host, port);
            messenger = new Messenger(serverSocket);
            messenger.send("CONNECTION");
            System.out.println(messenger.receivedMessage());
        }
        catch (IOException e) {
        }
    }
    
    private void endConnection() {
        messenger.send("QUIT");
        System.out.println(messenger.receivedMessage());
    }
}
