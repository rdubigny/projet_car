package client;

import data.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Client {
    /* attribute */

    private Messenger messenger;
    private static final EntryFormatter entryFormatter
            = new EntryFormatter(new Scanner(System.in));
    private static String login;


    /* method */
    public static void main(String[] args) {
        // read the config file
        Config.readConfigFile();

        try {
            Config.config.size();
            InetAddress tmpAddr, masterAddr;
            int tmpPort, masterPort;
            int id = 0;
            while (true) {
                tmpAddr = Config.config.get(id).getIpAddress();
                tmpPort = Config.config.get(id).getClientPort();
                try {
                    Messenger messenger = new Messenger(new Socket(tmpAddr, tmpPort));
                    messenger.send("WHEREISMASTER");
                    DataContainer resp = messenger.receive();
                    messenger.close();
                    if (resp.getContent().equals("MASTERIS")) {
                        int masterId = Integer.parseInt(resp.getDescription());
                        masterAddr = Config.config.get(masterId).getIpAddress();
                        masterPort = Config.config.get(masterId).getClientPort();
                        break;
                    }
                } catch (IOException e) {
                    System.out.println("Unable to connect to " + id);
                }
                id = (id + 1) % Config.config.size();
            }
            System.out.println("\nWelcome to your shared file system!");
            DataContainer command = entryFormatter.getLogin();
            login = command.getDescription();
            boolean keepGoing = true;
            while (keepGoing) {
                Client user;
                user = new Client(masterAddr, masterPort);
                int attempts = 0;
                while (!user.isConnected() && attempts <= 5) {
                    attempts++;
                    System.out.println("Trying to connect to our servers.");
                    TimeUnit.SECONDS.sleep(5);
                    user = new Client(masterAddr, masterPort);
                }

                if (user.isConnected()) {
                    boolean connexionHasFinished = false;
                    do {
                        System.out.print("> ");
                        command = entryFormatter.format();
                        if (command == null) {
                            System.out.println("Unknown command. Try: create, read, write, erase or quit.");
                        } else if (command.getContent().equals("EXIT")) {
                            keepGoing = false;
                            break;
                        } else {
                            connexionHasFinished = user.execute(command);
                        }
                    } while (!connexionHasFinished);
                    user.close();
                }
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace(System.out);
        }
    }

    /* constructor */
    public Client(InetAddress host, int port) {
        try {
            messenger = new Messenger(new Socket(host, port));
        } catch (IOException e) {
            System.out.println("Connection could not be established\n");
        }
    }

    /**
     *
     * @return false if messenger could not be built, else true.
     */
    private boolean isConnected() {
        return messenger != null;
    }

    private void close() {
        messenger.close();
    }

    /**
     *
     * @param command
     * @return false if the connection is finished, else true.
     */
    private boolean execute(DataContainer command) {
        /*
        DataContainer preReq = new DataContainer("CONNECT",
                login);
        messenger.send(preReq);
        */
        File file;
        switch (command.getContent()) {
            case "CREATE":
                // get an id list to send the file on
                messenger.send(command);
                DataContainer resp = messenger.receive();
                if (resp.getContent().equals("WRITEAT")) {
                    System.out.println("File created");
                    file = new File(command.getDescription());
                    try {
                        file.createNewFile();
                    } catch (IOException ex) {
                        System.out.println("Unable to create file localy");
                    }
                } else {
                    System.out.println(resp.getContent());
                }
                return true;
            case "WRITE":
                String fileName = command.getDescription();
                file = new File(fileName);
                // Verify the file exists with this name
                if (!file.exists() || !file.isFile()) {
                    System.out.println("Unable to find file localy");
                    return true;
                }
                DataContainer commandWrite = new DataContainer("WHEREISFILE",
                        fileName);
                messenger.send(commandWrite);
                DataContainer respWrite = messenger.receive();
                if (!respWrite.getContent().equals("WRITEAT")) {
                    System.out.println("Unable to find remote file");
                    return true;
                }
                IdList idList = (IdList) (respWrite.getData());
                try {
                    // Read in the bytes
                    InputStream is;
                    is = new FileInputStream(fileName);
                    int size = is.available();
                    // Create the byte array to hold the data
                    byte[] bytes = new byte[size];
                    for (int i = 0; i < size; i++) {
                        bytes[i] = ((byte) is.read());
                    }
                    is.close();
                    Archive archive = new Archive(fileName, bytes);
                    int dataServerId = idList.list.remove(0);
                    ArchiveAndList data = new ArchiveAndList();
                    data.archive = archive;
                    data.list = idList.list;
                    DataContainer commandWrite2 = new DataContainer("WRITE",
                            (Data) data);
                    Messenger mesData = new Messenger(new Socket(
                            Config.config.get(dataServerId).getIpAddress(),
                            Config.config.get(dataServerId).getClientPort()));
                    mesData.send(commandWrite2);
                    mesData.close();
                    file.delete();
                } catch (FileNotFoundException ex) {
                    System.out.println("Unable to find file localy");
                } catch (IOException ex) {
                    System.out.println("Unable to read file localy");
                }
                return true;
            case "READ":
                String fileNameRead = command.getDescription();
                DataContainer commandRead = new DataContainer("WHEREISFILE",
                        fileNameRead);
                messenger.send(commandRead);
                DataContainer respRead = messenger.receive();
                if (!respRead.getContent().equals("WRITEAT")) {
                    System.out.println("Unable to find remote file");
                    return true;
                }
                IdList idListRead = (IdList) (respRead.getData());
                int ran = (int) (Math.random() * idListRead.list.size());
                int dataServerId = idListRead.list.remove(ran);
                DataContainer commandRead2 = new DataContainer("READ", fileNameRead);
                Messenger mesData;
                try {
                    System.out.println("File read at "+dataServerId);
                    mesData = new Messenger(new Socket(
                            Config.config.get(dataServerId).getIpAddress(),
                            Config.config.get(dataServerId).getClientPort()));
                    mesData.send(commandRead2);
                    DataContainer respRead2 = mesData.receive();
                    Archive archive = (Archive) respRead2.getData();
                    file = new File(archive.getFileName());
                    if(file.createNewFile()){
                        file.delete();
                        file.createNewFile();
                    }
                    OutputStream os = new FileOutputStream(archive.getFileName());
                    os.write(archive.getBytes());
                    os.close(); 
                    mesData.close();
                } catch (IOException ex) {
                    ex.printStackTrace(System.out);
                }

                break;
            case "DELETE":
                break;
            // this one helps debuging
            case "WHERE":
                String fileNameWhere = command.getDescription();
                DataContainer commandWhere = new DataContainer("WHEREISFILE",
                        fileNameWhere);
                messenger.send(commandWhere);
                DataContainer respWhere = messenger.receive();
                if (!respWhere.getContent().equals("WRITEAT")) {
                    System.out.println("Unable to find remote file");
                    return true;
                }
                IdList idListWhere = (IdList) (respWhere.getData());
                System.out.println(fileNameWhere+" is at "+idListWhere.list.toString());
        }
        return true;
    }

    private void treatment(DataContainer datacontainer) {
        if (datacontainer.getContent().equals("FILE")) {
            Archive archive = (Archive) datacontainer.getData();
            try {
                if (archive != null) {
                    FileConverterArchive.getFileFromArchive(archive.getFileName(), archive.getBytes());
                } else {
                    System.out.println("File doesn't exist !");
                }
            } catch (IOException e) {
                System.out.println("Error file not created !");
            }
        }
    }

}
