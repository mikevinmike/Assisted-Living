/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.genetic;

/**
 * interface defining characteristics of a chromosome generator
 * @author mike
 */
public interface IChromosomeGenerator {
    
    /**
     * 
     * @return newly generated chromosome
     */
    public GeneList getNewChromosome();
    
}
