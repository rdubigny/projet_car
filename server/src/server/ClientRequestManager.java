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
          if (messenger.receivedMessage().equalsIgnoreCase("Connection")) {
              messenger.send("Client successfully connected to server.");          
              while (true) {
                  String command = messenger.receivedMessage();
                  System.out.println(command);
                  if(command.equalsIgnoreCase("Quit"))
                      break;
                  String answer = execute(command);
                  messenger.send(answer);
              }
          }
          finishConexion();
      }
      
      private void finishConexion() {
          messenger.send("Connexion was finished.");
          try {
              clientSocket.close();
          } catch (IOException ex) {
              Logger.getLogger(ClientRequestManager.class.getName()).log(Level.SEVERE, null, ex);
          }
      }
      
      private String execute(String command) {
          return "This command is not functional yet.";
      }
  }
    
