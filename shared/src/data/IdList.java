/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package data;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author scar
 */
public class IdList extends Data {
    private static final long serialVersionUID = 6529685098267754768L;

    public List<Integer> list;
    
    public IdList() {
        this.list = new LinkedList<>();
    }
        
}
