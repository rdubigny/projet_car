/**
 *
 * @author gyls
 */
package memory;


public abstract class Data {
        public int id;
        
        public Data(int id) {
            this.id = id;
        }
        
        public abstract void display();
}
