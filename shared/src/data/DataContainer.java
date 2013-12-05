/*
 * DataContainer is a Data Transfer Object used in the data transmission through 
 * the network client-server.
 */
package data;

import java.io.Serializable;


/**
 *
 * @author paulinod
 */
public class DataContainer implements Serializable {
    private String content;
    private String description;
    private Data data;
    
    public DataContainer(String content) {
        this.content = content;
    }
    
    public DataContainer(String content, String description) {
        this(content);
        this.description = description;
    }
    
    public DataContainer(String content, Data data) {
        this(content);
        this.data = data;
    }

    public String getContent() {
        return content;
    }

    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        String s = content + "\n";
        if (description != null) 
            s += description + "\n";
        if (data != null)
            s += data.toString();
        return s;
    }
    
    public void display() {
        System.out.println(toString());
    }
}