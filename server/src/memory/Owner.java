/**
 *
 * @author gyls
 */
package memory;


class FileOwner {
    int idFile;
    String nameFile;
}

public class Owner {
    private int id;
    private String nameOwner;
    private int timeoutConnexion;
    private String ip;
    
    /* constructor */
    public Owner(int id, String nameOwner,int timeoutConnexion, String ip) {
       this.id = id;
       this.nameOwner = nameOwner;
       this.timeoutConnexion = timeoutConnexion;
       this.ip = ip;
    };

    @Override
    public String toString() {
        return "(" + nameOwner + ", " + timeoutConnexion + ", " + ip + ")"; 
    }
}
