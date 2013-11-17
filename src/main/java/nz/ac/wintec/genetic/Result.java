/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.genetic;

/**
 * This class holds a chromosome, which is a valid result of the evolution
 * process
 *
 * @author mike
 */
public class Result {

    private Chromosome chromosome;
    private Evolution generation;
    private int chromosomeIndex;
    private int generationIndex;
    private float fitness;

    /**
     * @return the chromosome
     */
    public Chromosome getChromosome() {
        return chromosome;
    }

    /**
     * @param chromosome the chromosome to set
     */
    public void setChromosome(Chromosome chromosome) {
        this.chromosome = chromosome;
    }

    /**
     * @return the generation
     */
    public Evolution getGeneration() {
        return generation;
    }

    /**
     * @param generation the generation to set
     */
    public void setGeneration(Evolution generation) {
        this.generation = generation;
    }

    /**
     * @return the chromosomeIndex
     */
    public int getChromosomeIndex() {
        return chromosomeIndex;
    }

    /**
     * @param chromosomeIndex the chromosomeIndex to set
     */
    public void setChromosomeIndex(int chromosomeIndex) {
        this.chromosomeIndex = chromosomeIndex;
    }

    /**
     * @return the generationIndex
     */
    public int getGenerationIndex() {
        return generationIndex;
    }

    /**
     * @param generationIndex the generationIndex to set
     */
    public void setGenerationIndex(int generationIndex) {
        this.generationIndex = generationIndex;
    }

    /**
     * @return the fitness
     */
    public float getFitness() {
        return fitness;
    }

    /**
     * @param fitness the fitness to set
     */
    public void setFitness(float fitness) {
        this.fitness = fitness;
    }
}
