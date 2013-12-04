/*
 * ClientManager is responsible for the interaction with a connected client. It 
 * receives the requests, executes them and send the answers back.
 */
package server;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author paulinod
 */
public class ClientRequestManager implements Runnable {
      private final Socket clientSocket;
      private final Messenger messenger;
      private final Thread thread;

      ClientRequestManager(Socket socket) {    
          clientSocket = socket;
          messenger = new Messenger(clientSocket);
          thread = new Thread(this);
          thread.start();
      }

      @Override
      public void run() {
          String command;
          do {
              command = messenger.receivedMessage();
              System.out.println("Executing command " + command);
              execute(command);
          } while (! command.equals("QUIT"));
      }
      
      private void execute(String command) {
          if (command.equals("CONNECT"))
              messenger.send(" Client successfully connected to server.");
          else if (command.equals("QUIT"))
              finishConexion();
          
          // FIXME: implement create, read and write methods
          else if (command.equals("CREATE"))
              messenger.send("This command is not functional yet.");
          else if (command.equals("READ"))
              messenger.send("This command is not functional yet.");
          else if (command.equals("WRITE"))
              messenger.send("This command is not functional yet.");
          
          else messenger.send("Unknown command. Try: create, read, write or quit.");
      }
      
      private void finishConexion() {
          messenger.send("Connexion was finished.");
          try {
              clientSocket.close();
          } catch (IOException ex) {
              Logger.getLogger(ClientRequestManager.class.getName()).log(Level.SEVERE, null, ex);
          }
      }
}