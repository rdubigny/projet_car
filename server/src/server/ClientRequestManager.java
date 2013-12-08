/*
 * ClientRequestManager is responsible for the interaction with a connected 
 * client. It receives the requests, executes them and send the answers back.
 */
package server;

import data.*;
import memory.*;
import java.net.Socket;
import server.utils.*;

public class ClientRequestManager implements Runnable {

    private final Messenger messenger;
    private final Thread thread;
    private String login;
    private Memory memory;

    ClientRequestManager(Socket clientSocket) {
        messenger = new Messenger(clientSocket);
        memory = new Memory();
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
        switch (command) {
            case "QUIT":
                finishConnection();
                return true;
            case "CONNECT":
                login(parameter);
                break;
            case "CREATE":
                createFile((Archive) data);
                break;
            case "READ":
                readFile(parameter);
                break;
            case "WRITE":
                writeFile(parameter);
                break;
            case "ERASE":
                eraseFile(parameter);
                break;
            case "PREPARECREATE":
                registerFile(parameter);
                break;
            case "WHEREISFILE":
                locateFile(parameter);
                break;
            case "PREPAREERASE":
                unregisterFile(parameter);
                break;
            default:
                messenger.send("Unknown command. Try: create, read, write, erase or quit.");
                break;
        }

        return false;
    }

    private void login(String parameter) {
        login = parameter;
        messenger.send(" Client " + login + " successfully connected to server.");
    }

    private void createFile(Archive archive) {
        System.out.println("CREATE A FILE");
        System.out.println(login + "/" + archive.getFileName());
        memory.write("mem_tmp", login + "/" + archive.getFileName(), archive);
        messenger.send(archive.getFileName() + " written in the memory");
    }

    private void readFile(String fileName) {
        System.out.println("READ A FILE");
        System.out.println(login + "/" + fileName);
        Archive archive = memory.read("mem_tmp", login + "/" + fileName);
        messenger.send(new DataContainer("FILE", archive));
    }

    private void writeFile(String parameter) {
        messenger.send("This command is not functional yet.");
    }

    private void eraseFile(String fileName) {
        System.out.println("ERASE A FILE");
        System.out.println(login + "/" + fileName);
        Archive archive = memory.delete("mem_tmp", login + "/" + fileName);
        if (archive == null)
            messenger.send(fileName + " does not exist in the memory");
        else messenger.send(archive.getFileName() + " erased from the memory");
    }

    private void registerFile(String parameter) {
        if (Config.getInstance().IamTheMaster()) {
            Server.nameNodeManager.register(parameter);
        } else {
            messenger.send("Internal Error");
        }
    }

    private void locateFile(String parameter) {
        if (Config.getInstance().IamTheMaster()) {
            Server.nameNodeManager.getIds(parameter);
            // if ids == null error!
            messenger.send("WRITEAT");
        } else {
            messenger.send("Internal Error");
        }
    }

    private void unregisterFile(String parameter) {
        if (Config.getInstance().IamTheMaster()) {
            Server.nameNodeManager.unregister(parameter);
        } else {
            messenger.send("Internal Error");
        }
    }

    private void finishConnection() {
        messenger.send("Connexion was finished.");
        messenger.close();
    }
}
