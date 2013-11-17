/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.genetic;

import java.util.List;

/**
 * interface for defining characteristic of a gene container
 * @author mike
 */
public interface IGeneHolder {
    
    /**
     * 
     * @return list of genes 
     */
    public List<IGene> getGenes();
    
}
