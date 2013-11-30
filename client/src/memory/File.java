/**
 *
 * @author gyls
 */
package memory;

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
    
    public void display() {
        System.out.print(this.nameFile);
        System.out.print(this.idOwner);
        System.out.print(this.size);
        System.out.print(this.data);
    }
}
