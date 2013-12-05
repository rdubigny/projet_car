/**
 *
 * @author gyls
 */
package memory;

import data.Data;

public class File extends Data {
    public String nameFile;
    public int idOwner;
    public int size;
    public String data;
    
    public File(int id, String nameFile, int idOwner,
        int size, String data) {
       super(id);
       this.nameFile = nameFile;
       this.idOwner = idOwner;
       this.size = size;
       this.data = data;
    };
    
    
    @Override
    public String toString() {
        return "(" + nameFile + ", " + idOwner + ", " + size + ", " + data + ")"; 
    }
}
