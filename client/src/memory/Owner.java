/**
 *
 * @author gyls
 */
package memory;

import data.Data;
import java.util.*;

class FileOwner {
    int idFile;
    String nameFile;
}

public class Owner extends Data {
    public String nameOwner;
    public int timeoutConnexion;
    public String ip;
    
    /* constructor */
    public Owner(int id, String nameOwner,int timeoutConnexion, String ip) {
       super(id);
       this.nameOwner = nameOwner;
       this.timeoutConnexion = timeoutConnexion;
       this.ip = ip;
    };

    @Override
    public String toString() {
        return "(" + nameOwner + ", " + timeoutConnexion + ", " + ip + ")"; 
    }
}
