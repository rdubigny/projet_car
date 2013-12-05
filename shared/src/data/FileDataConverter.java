/*
 * FileDataConverter offers methods of conversion between Java files and Data.
 */
package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author paulinod
 */
public class FileDataConverter {
    private static final int maxSize = 5000000;
    
    public static Data toData(String fileName) {
        return toData(new File(fileName));
    }
    
    public static Data toData(File file) {
        String fileName = file.getName();
        System.out.println("Loading " + fileName);
        Data data = null;
        try {
            FileInputStream in = new FileInputStream(file);
            byte b[] = new byte[maxSize];
            int size = in.read(b, 0, b.length);
            data = new Data(fileName, b, size);
            in.close();
            
        } catch (IOException ex) {
            Logger.getLogger(FileDataConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    
    public static void toFile(Data file) {
        String fileName = file.getFileName();
        System.out.println("Saving " + fileName);
        try {
            FileOutputStream out = new FileOutputStream(fileName);
            out.write(file.getData(), 0, file.getSize());
            out.close();

        } catch (IOException ex) {
            Logger.getLogger(FileDataConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
