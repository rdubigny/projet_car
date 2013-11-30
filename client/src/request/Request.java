/**
 *
 * @author gyls
 */
package request;


import memory.*;

public class Request {
    private String order;
    private Data data;
    
    /* constructor */
    public Request(String order, Data data) {
        this.order = order;
        this.data  = data;
    }
    
    /* setter */
    void setOrder(String order) {
        this.order = order;
    }
  
     void setData(Data data) {
        this.data = data;
    }
  
    /* getter */
    String getOrder() {
        return this.order;
    }
    
    Object getData() {
        return this.data;
    }
}
