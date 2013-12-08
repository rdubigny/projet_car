/*
 * EntryFormatter creates DataContainers by formatting submitted data.
 */
package client;

import java.util.*;
import java.io.*;
import data.*;

public class EntryFormatter {

    private final Scanner scanner;

    public EntryFormatter(Scanner scanner) {
        this.scanner = scanner;
    }

    public DataContainer format() {
        String[] entry = scanner.nextLine().trim().split(" ");

        if (entry.length == 2) {
            String command = entry[0].toUpperCase();
            switch (command) {
                case "PREPARECREATE":
                    return new DataContainer(command, entry[1]);
                case "CREATE":
                case "WRITE":
                {
                    String fileName = entry[1];
                    try {
                        Archive archive = FileConverterArchive.getArchiveFromFile(fileName);
                    } catch (IOException e) {
                        return null;
                    }
                }
                case "READ":
                {
                    String fileName = entry[1];
                    return new DataContainer(command, fileName);
                }
            }
        } else if (entry.length == 1) {
            return new DataContainer(entry[0].toUpperCase());
        }
        return null;
    }

    public DataContainer getLogin() {
        System.out.print("Please type your login : ");
        String login = scanner.nextLine();
        return new DataContainer("CONNECT", login);
    }

}
