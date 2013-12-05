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

    public Data getData() {
        return data;
    }
    
    @Override
    public String toString() {
        String s = content + " ";
        if (description != null) 
            s += description;
        if (data != null)
            s += "\n" + data.toString();
        return s;
    }
    
    public void display() {
        System.out.println(toString());
    }
}