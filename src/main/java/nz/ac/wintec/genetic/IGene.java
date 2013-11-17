/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.genetic;

/**
 * interface for defining characteristics of a gene
 * @author mike
 */
public interface IGene extends Cloneable {
    
    /**
     * 
     * @return fitness value of gene
     */
    public float getFitnessValue();
    
    /**
     * 
     * @return a cloned gene 
     */
    public IGene getClonedGene();
    
    /**
     * 
     * @param geneNumber number of gene in chromosome
     * @return a generated gene
     * @throws Exception if no new gene could be generated
     */
    public IGene getNewGene(int geneNumber) throws Exception;

}
