/**
 *
 * @author gyls
 */
package data;


public abstract class Data {
        public int id;
        
        public Data(int id) {
            this.id = id;
        }
        
        @Override
        public abstract String toString();
        
        public void display() {
            System.out.println(toString());
        };
}
