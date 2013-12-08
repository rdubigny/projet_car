/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import java.util.HashMap;
import java.util.List;
import server.utils.Config;
import server.utils.Status;

/**
 *
 * @author car team 16
 */
public class NameNode extends Thread {

    private final boolean verbose;
    private HashMap<String, List> theNode;
    private HashMap<String, List> theTmpNode;

    public NameNode(boolean verbose) {
        this.verbose = verbose;
        theNode = new HashMap<>();
        theTmpNode = new HashMap<>();
    }

    @Override
    public void run() {
        while (true) {
            synchronized (Config.getInstance().lockStatus) {
                try {
                    Config.getInstance().lockStatus.wait();
                    if (Config.getInstance().getStatut() == Status.SECONDARY){
                        if (verbose) System.out.println("NameNode activated!");
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace(System.out);
                }
            }
        }
    }
    
    /**
     * 
     * @return zero if the process finished successfully
     */
    public int update() {
        System.out.println("nameNode.update");
        // TODO : update NameNode
        return 0;
    }
    
    public int create(String parameter, List<Integer> list) {
        if (theNode.containsKey(parameter)){
            return 1;
        }
        theTmpNode.put(parameter, list);
        if (verbose){
            String res = "";
            for (int i = 0; i < list.size(); i++) {
                 res += list.get(i)+", ";           
            }
            System.out.println("NameNode create file \""+parameter+"\" on servers: "+res);
        }
        return 0;
    }

    public void deliver(String parameter) {
        if (theTmpNode.containsKey(parameter)){
            theNode.put(parameter, theTmpNode.get(parameter));            
        }
        if (verbose) System.out.println("NameNode deliver \""+parameter+"\"");
    }
}
