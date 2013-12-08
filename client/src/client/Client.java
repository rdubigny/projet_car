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
    private static String login;
    
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
        login = command.getDescription();
        Client user = new Client(host, port);  
        boolean connexionHasFinished = user.execute(command);
        do {
            System.out.print("> ");
            command = entryFormatter.format();
            if(command!=null) {
                connexionHasFinished = user.execute(command);
            } else {
                System.out.println("Unknown command. Try: create, read, write, erase or quit.");
            }             
        } while (!connexionHasFinished);
        user.close();
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
            run("127.0.0.1", 1025);
    }
}
