/*
 * ServerRequestManager is responsible for the interaction with a connected 
 * client. It receives the requests, executes them and send the answers back.
 */
package server;

import data.Data;
import data.DataContainer;
import data.Messenger;
import java.net.Socket;
import server.utils.Config;
import server.utils.Status;

/**
 *
 * @author scar
 */
public class ServerRequestManager implements Runnable {

    private final Messenger messenger;
    private final Thread thread;

    ServerRequestManager(Socket serverSocket) {
        messenger = new Messenger(serverSocket);
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

        if (command.equals("UPDATE")) {
            update(parameter);
            messenger.close();
            return true;
        } else if (command.equals("SECONDARY")) {
            Config.getInstance().setStatus(Status.SECONDARY);
            messenger.close();
            return true;
        } else {
            messenger.send("Unknown message!");
        }

        return false;
    }

    private void update(String parameter) {
        if (Server.nameNode.update() == 0) {
            // TODO record updated nameNode
            messenger.send("OK");
            DataContainer request = messenger.receive();
            String command = request.getContent();
            if (command.equals("DELIVER")){
                // TODO put the temporary record in main memory
            }
        }
    }
}
