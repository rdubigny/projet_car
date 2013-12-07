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
        while(true) {
            String[] entry = scanner.nextLine().trim().split(" ");
            
            if (entry.length == 2) {
                /* TODO */
                if (entry[0].equals("CREATE"))
                return new DataContainer(entry[0].toUpperCase(), entry[1]);
            } else if (entry.length == 1) {
                return new DataContainer(entry[0].toUpperCase());
            }
            System.out.println("This command is not valid.");
        }
    }
    
    public DataContainer getLogin() {
        System.out.print("Please type your login : ");
        String login = scanner.nextLine();
        return new DataContainer("CONNECT", login);
    }
    

}
