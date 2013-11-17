/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.genetic;

import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import nz.ac.wintec.entity.Course;
import nz.ac.wintec.entity.Meal;

/**
 * This
 *
 * @author mike
 */
public class Population<T extends IChromosomeGenerator> {

    /* 
     * total fitness of chromosome;
     * in this case the fitness are the costs, 
     * so this is the total cost of the chromosome
     */
    private float totalFitness;
    /*
     * total reversed fitness of chromosome;
     * this value represents the total of the reversed fitness of each gene, 
     * meaning that a gene is fitter if it is cheaper 
     */
    private float totalReversedFitness;
    /*
     * holds the minimum fitness of this population, 
     * which is the best solution (since costs = fitness should be low)
     */
    private float minFitness = Float.MAX_VALUE;
    /*
     * best chromosome of this population
     */
    private Chromosome relativeBestSolution;
    /*
     * best chromosome in entire evolution process
     */
    private float averageFitness;
    /*
     * chromosomes of this population
     */
    private ArrayList<Chromosome> chromosomes = new ArrayList<Chromosome>();
    private IChromosomeGenerator chromosomeGenerator;

    public Population() {
    }

    /**
     * needed if random population should be generated
     *
     * @param chromosomeGenerator
     */
    public Population(T chromosomeGenerator) {
        this.chromosomeGenerator = chromosomeGenerator;
    }

    /**
     * creates random population, which is needed for the first generation
     *
     * @throws Exception if no chromosomeGenerator is set
     */
    public void createRandomPopulation() throws Exception {
        if (this.chromosomeGenerator == null) {
            throw new Exception("A chromosomeGenerator has to be defined to create a random population");
        }

        Evolution.resetMemoryOptimizedGeneration();
        Course.allMealCourses.clear();
        Meal.allMeals.clear();

        int count = 0;
        // produce as many chromosomes as specified in the settings
        while (count < GenerationSettings.getInstance().getPopulationSize()) {
            // use an IChromosomeGenerator to produce the next chromosome
            this.addChromosome(new Chromosome(chromosomeGenerator.getNewChromosome()));

            count++;
        }

        // when finished with populating, the fitness of all chromosomes will be calculated
        calculateTotalFitness();
    }

    /**
     * calculates total fitness of all chromosomes and of population
     */
    public void calculateTotalFitness() {
        resetTotalFitnessValues();

        // get minimumFitness first to be able to scale
        float totalFitness = 0;
        for (Chromosome chromosome : this.getChromosomes()) {
            float fitness = chromosome.getTotalFitness();
            // needed for average fitness
            totalFitness += chromosome.getTotalFitness();
            // minimum value needed for scaling
            if (fitness < minFitness) {
                minFitness = fitness;
            }
            // set the fittest chromosome in this population
            if (getRelativeBestSolution() == null || getRelativeBestSolution().getTotalFitness() > fitness) {
                setRelativeBestSolution(chromosome);
            }
            // set the fittest chromosome in the entire evolution process
            if (Evolution.getResult() == null || Evolution.getResult().getChromosome().getTotalFitness() > fitness) {
                setResult(chromosome);
            }
        }
        // set the average fitness value of all chromosomes
        setAverageFitness(totalFitness / this.getChromosomes().size());

        // calculate total fitness
        for (Chromosome chromosome : this.getChromosomes()) {
            // scale fitness to get bigger gaps between chromosome fitness values
            incrementTotalFitness(scaleFitness(chromosome.getTotalFitness()));
        }

        // calculate total reversed fitness to make cheaper chromosomes fitter later on
        for (Chromosome chromosome : this.getChromosomes()) {
            // reverse the fitness value by exponentiating by -1
            incrementTotalReversedFitness(1 / scaleFitness(chromosome.getTotalFitness()));
        }

    }

    /**
     * scales fitness value to generate bigger gaps between these idea from Andy
     * Fendall
     *
     * @param originalFitness total costs of chromosome
     * @return scaled fitness value
     */
    private float scaleFitness(float originalFitness) {
        // subtracts originalFitness by the minimum fitness value of this population
        // subtracts minFitness by 1 to avoid division by 0 later on when reversing
//        return (float) originalFitness - (minFitness-1);
        // works better withouth scaling, because then there is more variety for mating
        return (float) originalFitness;
    }

    /**
     * generates fitness relative to this population
     *
     * @param chromosome chromosome, of which the fitness should be calculated
     * @param reversed indicates if the value should be reversed or not
     * @return value between 0 and 1
     */
    public float getRelativeFitness(Chromosome chromosome, boolean reversed) {
        if (reversed) {
            // reverse the scaled fitness of chromosome by exponentiating by -1
            // then get the percentage of the chromosome's reversed fitness value
            return (1 / scaleFitness(chromosome.getTotalFitness())) / getTotalReversedFitness();
        } else {
            // get the percentage of the chromosome's scaled fitness value
            return scaleFitness(chromosome.getTotalFitness()) / getTotalFitness();
        }
    }

    /**
     * sets the result of the evolution process
     *
     * @param chromosome best solution chromosome
     */
    public void setResult(Chromosome chromosome) {

        Result result = new Result();
        result.setChromosome(chromosome);
        result.setChromosomeIndex(this.getChromosomes().indexOf(chromosome));
        result.setGeneration(Evolution.getCurrentGeneration());
        result.setGenerationIndex(Evolution.getNumberOfGenerations() - 1);
        result.setFitness(chromosome.getTotalFitness());

        Evolution.setResult(result);

        // termination criterion desired costs
        if (Evolution.getDesiredCosts() >= result.getFitness()) {
            Evolution.setStopEvolution(true);

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "desired costs reached",
                    "the desired costs of " + Evolution.getDesiredCosts() + " have been reached. Result: " + result.getFitness());

            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    /**
     * @return the chromosomes
     */
    public ArrayList<Chromosome> getChromosomes() {
        return chromosomes;
    }

    /**
     * @param chromosomes the chromosomes to set
     */
    public void addChromosome(Chromosome chromosome) {
        this.chromosomes.add(chromosome);
    }

    /**
     *
     * @return the total fitness of this population
     */
    public float getTotalFitness() {
        return totalFitness;
    }

    /**
     *
     * @return the total reversed fitness of this population
     */
    public float getTotalReversedFitness() {
        return totalReversedFitness;
    }

    /**
     * sets values of totalFitness and totalReversedFitness to 0
     */
    private void resetTotalFitnessValues() {
        this.totalFitness = 0;
        this.totalReversedFitness = 0;
    }

    /**
     * increases total fitness
     *
     * @param value value, by which the total fitness should be increased
     */
    private void incrementTotalFitness(float value) {
        this.totalFitness += value;
    }

    /**
     * increases total reversed fitness
     *
     * @param value value, by which the total fitness should be increased
     */
    private void incrementTotalReversedFitness(float value) {
        this.totalReversedFitness += value;
    }

    /**
     * @return the relativeBestSolution
     */
    public Chromosome getRelativeBestSolution() {
        return relativeBestSolution;
    }

    /**
     * @param relativeBestSolution the relativeBestSolution to set
     */
    public void setRelativeBestSolution(Chromosome relativeBestSolution) {
        this.relativeBestSolution = relativeBestSolution;
    }

    /**
     * @return the averageFitness
     */
    public float getAverageFitness() {
        return averageFitness;
    }

    /**
     * @param averageFitness the averageFitness to set
     */
    public void setAverageFitness(float averageFitness) {
        this.averageFitness = averageFitness;
    }
}
