package client;

import data.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Client {
    /* attribute */
    private Messenger messenger;
    private static final EntryFormatter entryFormatter = 
            new EntryFormatter(new Scanner(System.in));
    private static String login;
    
    /* constructor */
    public Client(String host, int port) {
        try {
            messenger = new Messenger(new Socket(host, port));
        } catch (IOException e) {
            System.out.println("Connection could not be established\n");
        }
    }
    
    /* method */
    private static void run(String tmpHost, int tmpPort) throws InterruptedException {
        String host = tmpHost; // change to new ip adress
        int port = 0; 
        while(port == 0){
            try {
                Messenger messenger = new Messenger(new Socket(tmpHost, tmpPort));
                messenger.send("WHEREISMASTER");
                DataContainer resp = messenger.receive();
                messenger.close();
                if (resp.getContent().equals("MASTERISAT")){
                    port = Integer.parseInt(resp.getDescription());
                    continue;
                }
            } catch (IOException e) {
                System.out.println("Unable to find master\n");
            }
            TimeUnit.SECONDS.sleep(5);            
        }
        System.out.println("\nWelcome to your shared file system!");
        DataContainer command = entryFormatter.getLogin();
        login = command.getDescription();
        Client user = new Client(host, port);
        int attempts = 0;
        while(!user.isConnected() && attempts <= 5) {
            attempts++;
            System.out.println("Trying to connect to our servers.");
            TimeUnit.SECONDS.sleep(5);
            user = new Client(host, port);
        }
        
        if (user.isConnected()) {
            boolean connexionHasFinished = user.execute(command);
            do {
                System.out.print("> ");
                command = entryFormatter.format();
                if(command != null) {
                    connexionHasFinished = user.execute(command);
                } else 
                    System.out.println("Unknown command. Try: create, read, write, erase or quit.");       
            } while (!connexionHasFinished);
            user.close();
        }
    }
    
    
    /**
     * 
     * @return false if messenger could not be built, else true. 
     */
    private boolean isConnected() {
        return messenger != null;
    }
    
    /**
     * 
     * @param command
     * @return false if the connection is finished, else true. 
     */
    private boolean execute(DataContainer command) {
        // client sends its command
        messenger.send(command);
        // client wait for a response
        DataContainer received = messenger.receive();
        // client treats datum 
        treatment(received);
        System.out.println(received.getContent());
        return received.getContent().equals("Connection was finished.");
    }
    
    
    private void treatment(DataContainer datacontainer) {
        if(datacontainer.getContent().equals("FILE")) {
            Archive archive = (Archive)datacontainer.getData();
            try {
                if(archive!=null) {
                    FileConverterArchive.getFileFromArchive(archive.getFileName(), archive.getBytes());
                } else {
                    System.out.println("File doesn't exist !");
                }
            } catch(IOException  e) {
                System.out.println("Error file not created !");
            }    
          }
    }
    
    private void close() {
        messenger.close();
    }
    
    /* main */
    public static void main(String[] args) {
        while (true)
            try {
                run("127.0.0.1", 1025);
            } catch (InterruptedException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
}
