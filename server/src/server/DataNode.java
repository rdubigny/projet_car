package server;


public class DataNode  extends Thread {
    private final boolean verbose;
        
    public DataNode(boolean verbose) {
        this.verbose = verbose;
    }
        
    @Override
    public void run() {
        while (true) {
            if (this.verbose)
                System.out.println("DataNode: I'm alive");
        }
    }
    
}
