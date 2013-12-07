package server;


public class DataNodeManager  extends Thread {
    private final boolean verbose;
        
    public DataNodeManager(boolean verbose) {
        this.verbose = verbose;
    }
        
    @Override
    public void run() {
        while (true) {
            if (this.verbose)
                System.out.println("DataNodeManager: I'm alive");
        }
    }
    
}
