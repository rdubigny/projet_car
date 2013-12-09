/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package data;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author scar
 */
public class NameNode extends Data{
    
    public HashMap<String, List> node;

    public NameNode(HashMap<String, List> node) {
        this.node = node;
    }
    
}
