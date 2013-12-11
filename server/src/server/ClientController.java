/*
 * ClientController is responsible for the interaction with a connected 
 * client. It receives the requests, executes them and send the answers back.
 */
package server;

import data.*;
import java.net.Socket;
import server.utils.*;

public class ClientController implements Runnable {

    private final Messenger messenger;
    private final Thread thread;
    private String login;

    ClientController(Socket clientSocket) {
        messenger = new Messenger(clientSocket);
        thread = new Thread(this);
        thread.start();
        login = "memory";
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
            case "CONNECT":
                login(parameter);
                return false;
            case "CREATE":
                registerFile(parameter);
                return true;
            case "READ":
                readFile(parameter);
                return true;
            case "WRITE":
                writeFile(data);
                return true;
            case "ERASE":
                eraseFile(parameter);
                break;
            case "WHEREISFILE":
                locateFile(parameter);
                return true;
            case "PREPAREERASE":
                unregisterFile(parameter);
                return true;
            case "WHEREISMASTER":
                whereismaster();
                return true;
            default:
                messenger.send("Unknown command. Try: create, read, write, erase or quit.");
                break;
        }

        return false;
    }

    private void login(String parameter) {
        login = parameter;
    }

    private void readFile(String fileName) {
        System.out.println("READ A FILE");
        System.out.println(login + "/" + fileName);
        Archive archive = Server.dataNode.memory.mem.get(login + "/" + fileName);
        messenger.send(new DataContainer("FILE", archive));
    }

    private void writeFile(Data data) {
        Server.dataNode.writeMultiple(data, login);
        messenger.close();
    }

    private void eraseFile(String fileName) {
        System.out.println("ERASE A FILE");
        System.out.println(login + "/" + fileName);
        Archive archive = Server.dataNode.memory.mem.remove(login + "/" + fileName);
        if (archive == null)
            messenger.send(fileName + " does not exist in the memory");
        else messenger.send(archive.getFileName() + " erased from the memory");
    }

    private void registerFile(String parameter) {
        if (parameter == null){
            messenger.send("Wrong parameter.");
        }else if (parameter.isEmpty()){
            messenger.send("Wrong parameter.");
        } else if (Config.getInstance().IamTheMaster()) {
            IdList idList;
            idList = Server.nameNodeManager.register(parameter);
            if (idList == null){
                messenger.send("Internal error. The file may already exists.");                
            }else if (idList.list.isEmpty()){
                messenger.send("Not enough server. Try again in a while.");
            } else {
                DataContainer resp = new DataContainer("WRITEAT", (Data)idList);
                messenger.send(resp);
            }
        } else {
            messenger.send("Internal Error");
        }
    }

    private void locateFile(String parameter) {
        if (Config.getInstance().IamTheMaster()) {
            IdList idList;
            idList = Server.nameNode.getIds(parameter);
            if (idList == null){
                messenger.send("Internal error. The file may not exists."); 
            } else {
                DataContainer resp = new DataContainer("WRITEAT", (Data)idList);
                messenger.send(resp);
            }
        } else {
            messenger.send("Internal Error");
        }
    }

    private void unregisterFile(String parameter) {
        if (Config.getInstance().IamTheMaster()) {
            Server.nameNodeManager.unregister(parameter);
            messenger.send("OK"); 
        } else {
            messenger.send("Internal Error");
        }
    }

    private void finishConnection() {
        messenger.send("Connection was finished.");
        messenger.close();
    }

    private void whereismaster() {
        DataContainer resp;
        int masterId = Config.getInstance().getMaster().getId();
        resp = new DataContainer("MASTERIS", String.valueOf(masterId));
        messenger.send(resp);
        messenger.close();
    }
}
