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
                case "CREATE":
                    return new DataContainer(command, entry[1]);
                case "WRITE":
                    return new DataContainer(command, entry[1]);
                case "READ":
                    return new DataContainer(command, entry[1]);
                case "DELETE":
                    return new DataContainer(command, entry[1]);
            }
        } else if (entry.length == 1) {
            String command = entry[0].toUpperCase();
            switch (command) {
                case "EXIT":
                    return new DataContainer("EXIT");
            }
        }
        return null;
    }

    public DataContainer getLogin() {
        System.out.print("Please type your login : ");
        String login = scanner.nextLine();
        return new DataContainer("CONNECT", login);
    }

}
