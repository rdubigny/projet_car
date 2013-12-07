package client;

import data.*;
import java.io.*;

public class FileConverterArchive {
       
    public static Archive getArchiveFromFile(String fileName) throws IOException {
        // Verify the file exists with this name
        File file = new File(fileName);
        if(!file.exists() || !file.isFile() )
            return null;
        // Read in the bytes
        InputStream is = new FileInputStream(fileName);
        int size = is.available();
        // Create the byte array to hold the data
        byte[] bytes = new byte[size];
        for(int i=0; i< size; i++) {
            bytes[i] = ((byte)is.read());
        }
        is.close();
        return new Archive(fileName, bytes);
    }
    
    public static void getFileFromArchive(String fileName, byte[] bytes) throws IOException {
        File file = new File(fileName);
        if(file.createNewFile()){
            file.delete();
            file.createNewFile();
        }
        OutputStream os = new FileOutputStream(fileName);
        os.write(bytes);
        os.close();    
    }
    
}
