package data;

public class Archive extends Data {
    /* attribute */
    public String fileName;
    public byte[] bytes;
    
    /* constructor */
    public Archive(String fileName, byte[] bytes) {
       this.fileName = fileName;
       this.bytes = bytes;
    };
    
    /* getter */
    public String getFileName() {
        return fileName;
    }
    
    public byte[] getBytes() {
        return bytes;
    }
    
    /* method */
    @Override
    public String toString() {
        return "(" + fileName + ")"; 
    }
    
    public void display() {
        if(this != null) {
            System.out.println(this.toString());
        } else  {
            System.out.println("null");
        }
    }   
    
}