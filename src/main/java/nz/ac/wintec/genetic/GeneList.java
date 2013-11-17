/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.genetic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ArrayList, which contains genes
 * @author mike
 */
public class GeneList<T extends IGeneHolder> extends ArrayList implements IGeneHolder {
    
    @Override
    public List<IGene> getGenes() {
        List<IGene> chromosomes = new ArrayList<IGene>();
        Iterator<T> iter = this.iterator();
        while(iter.hasNext()) {
            chromosomes.addAll( iter.next().getGenes() );
        }
        return chromosomes;
    }
    
    @Override
    public String toString() {
        String output = "";
        for(Object object : this) {
            output += (output.isEmpty() ? "" : " | ") + object.toString();
        }
        return output;
    }
    
}
