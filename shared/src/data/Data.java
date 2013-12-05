/**
 *
 * @author gyls
 */
package data;

import java.io.Serializable;

public class Data implements Serializable {
    public String fileName;
    public int id;
    public int idOwner;
    public int size;
    public byte[] data;
    
    public Data(String fileName, byte[] data, int size) {
       this.fileName = fileName;
       this.data = data;
       this.size = size;
    };
    
    public Data(int id, String fileName, int idOwner,
        int size, byte[] data) {
       this(fileName, data, size);
       this.id = id;
       this.idOwner = idOwner;
    };

    public String getFileName() {
        return fileName;
    }
    
    public byte[] getData() {
        return data;
    }

    public int getSize() {
        return size;
    }
    
    @Override
    public String toString() {
        return "(" + fileName + ", " + idOwner + ", " + size + ")"; 
    }
    
    public void display() {
        System.out.println(toString());
    }
}
