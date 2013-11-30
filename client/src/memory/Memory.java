/**
 *
 * @author gyls
 */
package memory;

import java.util.*;

public class Memory {
    
   public static HashMap mem;
   
   public Memory() {
       Memory.mem = new HashMap();
   }
   
   public void insert(Data data) {
       Object oldData = Memory.mem.put(data.id, data);
       if(oldData != null) {
           System.out.println("C'est une insertion qui supprime !");
           // on remet l'ancienne valeur
           Object o = Memory.mem.put(data.id, oldData);
       }
   }
   
   public Data search(int id) {
       Object data = mem.get(id);
              if(data == null) {
           System.out.println("Aucune valeur trouvée !");
       }
       return((Data)data);
   }
   
   public void delete(int id) {
       Object data = mem.remove(id);
       if(data == null) {
           System.out.println("Aucune valeur supprimée !");
       }
   }
   
   public void display() {
       Set entries = Memory.mem.entrySet();
       Iterator it = entries.iterator();
       while(it.hasNext()) {
           Map.Entry entry = (Map.Entry)it.next();
           Object key = entry.getKey();
           Object value = entry.getValue();
           Data data = (Data)value;
           data.display();
           }
       }
  
   }
