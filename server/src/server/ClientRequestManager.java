/*
 * ClientRequestManager is responsible for the interaction with a connected 
 * client. It receives the requests, executes them and send the answers back.
 */
package server;

import data.Data;
import data.DataContainer;
import data.Messenger;
import java.net.Socket;
import server.utils.Config;


/**
 *
 * @author paulinod
 */
public class ClientRequestManager implements Runnable {
      private final Messenger messenger;
      private final Thread thread;

      ClientRequestManager(Socket clientSocket) {
          messenger = new Messenger(clientSocket);
          thread = new Thread(this);
          thread.start();
      }

      @Override
      public void run() {
          DataContainer request;
          boolean hasFinished;
          do {
              request = messenger.receive();
              System.out.println("Executing command: " + request);
              hasFinished = execute(request);
          } while (!hasFinished);
      }
      
      private boolean execute(DataContainer request) {
          String command = request.getContent();
          String parameter = request.getDescription();
          Data data = request.getData();
          
          if (command.equals("QUIT")) {
              finishConexion();
              return true;
          }
          
          else if (command.equals("CONNECT"))
              login(parameter);
          else if (command.equals("CREATE"))
              createFile(data);
          else if (command.equals("READ"))
              readFile(parameter);
          else if (command.equals("WRITE"))
              writeFile(parameter);
          else if (command.equals("ERASE"))
              eraseFile(parameter);
          else messenger.send("Unknown command. Try: create, read, write, erase or quit.");
          
          return false;
      }
      
      private void login(String parameter) {
          messenger.send(" Client " + parameter + " successfully connected to server.");
      }
      
      // FIXME: implement create, read, write and erase methods
      private void createFile(Data data) {
          messenger.send("This command is not functional yet.");
      }
      
      private void readFile(String parameter) {
          messenger.send("This command is not functional yet.");          
      }
      
      private void writeFile(String parameter) {
          if (! Config.getInstance().IamTheMaster()) {
              messenger.send("This command is not functional for data servers yet.");
          } else {
              Server.nameNodeManager.update();
              messenger.send("WRITEAT");
          }
      }
      
      private void eraseFile(String parameter) {
          messenger.send("This command is not functional yet.");          
      }
      
      private void finishConexion() {
          messenger.send("Connexion was finished.");
          messenger.close();
      }
}