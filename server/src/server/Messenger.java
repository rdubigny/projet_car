/*
 * Messenger is an interface created in order to facilitate the communication 
 * client-server.
 */
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author paulinod
 */
public class Messenger {
    private Socket socket;
    private BufferedReader receivedMessage;
    private PrintStream sendingMessage;
    
    
    public Messenger(Socket s) {
        socket = s;
        try {        
            sendingMessage = new PrintStream(socket.getOutputStream());
            receivedMessage = new BufferedReader(new InputStreamReader(
                                            socket.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(Messenger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String receivedMessage() {
        try {        
            return receivedMessage.readLine();
        } catch (IOException ex) {
            Logger.getLogger(Messenger.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void send(String message) {
        sendingMessage.println(message);
    }
}
