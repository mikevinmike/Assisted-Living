/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.genetic;

import nz.ac.wintec.entity.Course;

/**
 * settings for evolution process
 * @author mike
 */
public class GenerationSettings {
    
    // single instance of this class
    private static GenerationSettings instance;
    // number of weeks the chromosome should consist of
    private int numberOfWeeks = 1;
    // class, which should be the gene; needs future development to work, at the moment only Course is possible
    private Class geneClass; 
    
    // crossover probability for evolution process
    private float crossoverProbability = 0.8f;
    // mutation probability for evolution process
    private float mutationProbability = 0.001f;
    // number of generations the should consist of evolution process
    private int generationsNumber = 300;
    // number of chromsomes a population should consist of
    private int populationSize = 500;
    // number of minutes the evolution process has time
    private float maxMinutes = 10;
    // decision about keeping old generations for statistics or not
    private boolean dismissOldGenerations = false;
    
    /**
     * sets class Course automatically as gene class 
     */
    private GenerationSettings() {
        setGeneClass(Course.class);
    }
    
    /**
     * 
     * @return single instance of GenerationSettings
     */
    public static GenerationSettings getInstance() {
        if(instance==null) {
            instance = new GenerationSettings();
        }
        return instance;
    }
    
    /**
     * @return the numberOfDays
     */
    public int getNumberOfDays() {
        return numberOfWeeks * 7;
    }

    /**
     * @return the numberOfWeeks
     */
    public int getNumberOfWeeks() {
        return numberOfWeeks;
    }

    /**
     * @param numberOfWeeks the numberOfWeeks to set
     */
    public void setNumberOfWeeks(int numberOfWeeks) {
        this.numberOfWeeks = numberOfWeeks;
    }

    /**
     * @return the geneClass
     */
    public Class getGeneClass() {
        return geneClass;
    }

    /**
     * @param geneClass the geneClass to set
     */
    public void setGeneClass(Class geneClass) {
        this.geneClass = geneClass;
    }

    /**
     * @return the crossoverProbability
     */
    public float getCrossoverProbability() {
        return crossoverProbability;
    }

    /**
     * @param crossoverProbability the crossoverProbability to set
     */
    public void setCrossoverProbability(float crossoverProbability) {
        this.crossoverProbability = crossoverProbability;
    }

    /**
     * @return the mutationProbability
     */
    public float getMutationProbability() {
        return mutationProbability;
    }

    /**
     * @param mutationProbability the mutationProbability to set
     */
    public void setMutationProbability(float mutationProbability) {
        this.mutationProbability = mutationProbability;
    }

    /**
     * @return the generationsNumber
     */
    public int getGenerationsNumber() {
        return generationsNumber;
    }

    /**
     * @param generationsNumber the generationsNumber to set
     */
    public void setGenerationsNumber(int generationsNumber) {
        this.generationsNumber = generationsNumber;
    }

    /**
     * @return the populationSize
     */
    public int getPopulationSize() {
        return populationSize;
    }

    /**
     * @param populationSize the populationSize to set
     */
    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    /**
     * @return the maxMinutes
     */
    public float getMaxMinutes() {
        return maxMinutes;
    }

    /**
     * @param maxMinutes the maxMinutes to set
     */
    public void setMaxMinutes(float maxMinutes) {
        this.maxMinutes = maxMinutes;
    }
    
    /**
     * @return the maxMinutes
     */
    public long getMaxMinutesInMilliseconds() {
        return (long) (maxMinutes * 60 * 1000);
    }

    /**
     * 
     * @return if old generations should be dismissed
     */
    public boolean isDismissOldGenerations() {
        return dismissOldGenerations;
    }

    /**
     * 
     * @param dismissOldGenerations if old generations should be dismissed
     */
    public void setDismissOldGenerations(boolean dismissOldGenerations) {
        this.dismissOldGenerations = dismissOldGenerations;
    }
    
}
