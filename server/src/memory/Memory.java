package memory;

import data.*;
import java.util.*;

public class Memory {

    private static HashMap<String, Archive> mem;
    private static HashMap<String, Archive> mem_tmp;

    public Memory() {
        mem = new HashMap<>();
        mem_tmp = new HashMap<>();
    }
    
    /**
     *
     * if the key is already used then the file is erased.
     *
     * @param type_mem the type of memory
     * @param key the name of the file in the memory is login_fileName and for
     * the user is only fileName.
     * @param file the value to insert.
     */
    public void write(String type_mem, String key, Archive file) {
        if (type_mem.equals("mem")) {
            mem.put(key, file);
        } else {
            mem_tmp.put(key, file);
        }
    }

    /**
     *
     * @param type_mem the type of memory
     * @param key the name of the file in the memory is login_fileName and for
     * the user is only fileName.
     * @return if it existed the previous value of Archive for this key, else
     * null.
     */
    public Archive delete(String type_mem, String key) {
        if (type_mem.equals("mem")) {
            return mem.remove(key);
        } else {
            return mem_tmp.remove(key);
        }
    }

    /**
     *
     * @param type_mem the type of memory
     * @param key the name of the file in the memory is login_fileName and for
     * the user is only fileName.
     * @return the file if it exists else return null.
     */
    public Archive read(String type_mem, String key) {
        if (type_mem.equals("mem")) {
            return mem.get(key);
        } else {
            return mem_tmp.get(key);
        }
    }

    public void display(String type_mem) {
        Set<Map.Entry<String, Archive>> entries;
        if (type_mem.equals("mem")) {
            entries = mem.entrySet();
        } else {
            entries = mem_tmp.entrySet();
        }
        for (Map.Entry entry : entries) {
            String key = (String) entry.getKey();
            Archive file = (Archive) entry.getValue();
            System.out.println(key);
        }
    }


  }
