/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.genetic;

import java.util.List;

/**
 * Represents a chromosome in terms of genetic algorithms
 * @author mike
 */
public class Chromosome {
    
    /* 
     * total fitness of chromosome;
     * in this case the fitness are the costs, 
     * so this is the total cost of the chromosome
     */
    private float totalFitness;
    // List of genes of which the chromosome consists
    private GeneList geneList;
    
    /**
     * chromosome must receive a GeneList when initialising
     * @param geneList 
     */
    public Chromosome(GeneList geneList) {
        this.geneList = geneList;
        
        // calculate Fitness of received GeneList
        calculateTotalFitness();
    }
    
    /**
     * calculates total fitness of chromosomes, that means it sums up the costs of all genes
     */
    public void calculateTotalFitness() {
        resetTotalFitness();

        for (IGene gene : (List<IGene>) this.getGeneList().getGenes()) {
            incrementTotalFitness(gene.getFitnessValue());
        }
    } 
    
    /**
     * 
     * @return total fitness of chromosome
     */
    public float getTotalFitness() {
        return totalFitness;
    }

    /**
     * resets total fitness of chromosome
     */
    private void resetTotalFitness() {
        this.totalFitness = 0;
    }

    /**
     * increases total fitness
     * @param value value, by which the total fitness should be increased
     */
    private void incrementTotalFitness(float value) {
        this.totalFitness += value;
    }

    /**
     * @return the genes
     */
    public GeneList getGeneList() {
        return geneList;
    }
    
    /**
     * generates chromosome string
     * @return chromosome string
     */
    public String getChromosomeString() {
        return "[ " + this.getGeneList().toString() + " ]";
    }
    
    @Override
    public String toString() {
        return this.getChromosomeString();
    }
}
