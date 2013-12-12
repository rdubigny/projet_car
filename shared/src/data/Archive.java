package data;

public class Archive extends Data {
    private static final long serialVersionUID = 6529685098267754752L;
    /* attribute */
    private String fileName;
    private String owner;
    private byte[] bytes;
    
    /* constructor */
    public Archive(String fileName, byte[] bytes) {
       this.fileName = fileName;
       this.bytes = bytes;
    };
    
    /* getter */
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getOwner() {
        return owner;
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