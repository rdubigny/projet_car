/*
 * Messenger is an interface created in order to facilitate the communication 
 * client-server.
 */
package data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author paulinod
 */
public class Messenger {
    private final Socket socket;
    private ObjectInputStream receivedMessage;
    private ObjectOutputStream sendingMessage;
    
    
    public Messenger(Socket s) {
        socket = s;
        try {        
            sendingMessage = new ObjectOutputStream(socket.getOutputStream());
            receivedMessage = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(Messenger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public DataContainer receive() {
        try {        
            return (DataContainer)receivedMessage.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Messenger.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void send(String message) {
        send(new DataContainer(message));
    }
    
    public void send(DataContainer message) {
        try {
            sendingMessage.reset();
            sendingMessage.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(Messenger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void close() {
        try {
            receivedMessage.close();
            sendingMessage.close();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Messenger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
