/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.genetic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import nz.ac.wintec.bean.EvolutionController;
import nz.ac.wintec.entity.Course;
import nz.ac.wintec.entity.Meal;
import nz.ac.wintec.entity.MealGenerator;
import nz.ac.wintec.entity.MealType;
import nz.ac.wintec.service.Logger;
import nz.ac.wintec.service.PersistenceService;
import nz.ac.wintec.service.RandomService;

/**
 * class responsible for the evolution process
 *
 * @author mike
 */
public class Evolution implements Cloneable {

    // list containg all generations of the evolution process
    private static ArrayList<Evolution> generations = new ArrayList<Evolution>();
    // holds the result of the evolution process
    private static Result result;
    // holds the popultion of this generation
    private Population population;
    // emergency stop for evolution process
    private static boolean stopEvolution = false;
    
    private static float desiredCosts;
    
    private static final float MAX_USED_MEMORY_PERCENT = 0.8f;
    private static int memoryOptimizedGeneration = 0;

    /**
     * adds this generation to the list of all generations
     */
    public Evolution() {
        generations.add(this);

        init();
    }
    /**
     * adds this generation to the list of all generations
     */
    public Evolution(int evolutionIndex) {
        generations.set(evolutionIndex, this);

        init();
    }

    /**
     * creates an initial population if there it is the first generation
     */
    private void init() {
        if (generations.size() == 1) {
            Population population = new Population(MealGenerator.getInstance());

            try {
                // create a random population
                population.createRandomPopulation();
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(Evolution.class.getName()).log(Level.SEVERE, null, ex);
            }

            this.setPopulation(population);
        }
    }

    /**
     *
     * @return most recent generation in evolution process
     */
    public static Evolution getCurrentGeneration() {
        // generates an initial generation if no one exists
        if (generations.isEmpty()) {
            new Evolution();
        }

        return generations.get(generations.size() - 1);
    }

    /**
     *
     * @return all generations of the evolution process
     */
    public static ArrayList<Evolution> getAllGenerations() {
        return generations;
    }

    /**
     * resets all the evolution process and creates an initial generation
     */
    public static void resetGenerations() {
        generations = new ArrayList<Evolution>();
        // delete the results
        setResult(null);
        // produce the initial population again
        new Evolution();
    }

    /**
     *
     * @return number of generations in evolution process
     */
    public static int getNumberOfGenerations() {
        return generations.size();
    }

    /**
     * generate as many generations as specified in the GenerationSettings
     */
    public static void runEvolution() {

        // reset stop indicator
        setStopEvolution(false);

        // get settings
        GenerationSettings settings = GenerationSettings.getInstance();
        
        // time keeping
        long startTime, currentTime, endTime;
        startTime = new Date().getTime();
        endTime = startTime + settings.getMaxMinutesInMilliseconds();

        /* run Evolution as long as it is not interrupted by the user or
         * as long as no termination criterion is satisfied
         * termination criteria: number of generations
         *                       time elapsed
         *                       desired costs reached (in Population.setResult())
         *   all these are defined by the user
        */        
        for (int generationIndex = getNumberOfGenerations();
                generationIndex < settings.getGenerationsNumber()
                && endTime > (currentTime = new Date().getTime())
                && !isStopEvolution();
                generationIndex++) {
            
            createNewGeneration();
            PersistenceService.getManagedBeanInstance(EvolutionController.class).setEvolutionProgress((generationIndex + 1f) / settings.getGenerationsNumber());
            PersistenceService.getManagedBeanInstance(EvolutionController.class).setTimeProgress((currentTime - startTime + 1f) / settings.getMaxMinutesInMilliseconds());
            PersistenceService.getManagedBeanInstance(EvolutionController.class).setFreeMemoryProgress(memoryOptimizedGeneration!=0 ? (memoryOptimizedGeneration)/(getNumberOfGenerations()-1f) : 0);
            Logger.log("Evolution", "generation #" + generationIndex + " created");
        }

        if (endTime <= new Date().getTime()) {
            PersistenceService.getManagedBeanInstance(EvolutionController.class).setTimeProgress(1);
        }
    }
    
    /**
     *
     * @return a new generation
     */
    public static Evolution createNewGeneration() {

        try {
            // clean up system to free memory
            // @link http://viralpatel.net/blogs/getting-jvm-heap-size-used-memory-total-memory-using-java-runtime/
            Runtime runtime = Runtime.getRuntime();
            float usedMemory = 0;
            
            // percentage of used memory
            if ((usedMemory = (runtime.totalMemory() - runtime.freeMemory() * 1f) / runtime.maxMemory()) > MAX_USED_MEMORY_PERCENT) {
                Logger.fatal("used memory", (usedMemory*100) + "%");
                
                for (; memoryOptimizedGeneration < getNumberOfGenerations(); memoryOptimizedGeneration++) {
                    for (Object chromosomeObject : getAllGenerations().get(memoryOptimizedGeneration).getPopulation().getChromosomes()) {
                        // make sure that no duplicate exists but that the same object is referenced
                        substitueWithRedundancy((Chromosome) chromosomeObject);
                    }
                    PersistenceService.getManagedBeanInstance(EvolutionController.class).setFreeMemoryProgress(memoryOptimizedGeneration!=0 ? (memoryOptimizedGeneration)/(getNumberOfGenerations()-1f) : 0);
                }
                memoryOptimizedGeneration--;
                
                System.gc();
               
            }

            /*
             * create new population out of the current one
             * must be done before initialising a new generation, 
             * because after 'new Evolution()' the current generation is the blank one, 
             * which leads to problem without population
             */
            Population newPopulation = Evolution.getCurrentGeneration().createNewPopulation();

            // creates blank generation
            Evolution newGeneration = new Evolution();

            // sets the population to the new generation
            newGeneration.setPopulation(newPopulation);

            if(GenerationSettings.getInstance().isDismissOldGenerations()) {
                // substitute generation with empty one (no statistics available)
                Evolution.createEmptyEvolution(getNumberOfGenerations()-2);
            }

            return newGeneration;

        } catch (Exception ex) {
            Logger.fatal("creation of population failed", ex.getMessage());
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * generates a new population using the current one
     *
     * @return newly generated population
     */
    private Population createNewPopulation() throws Exception {
        Population newPopulation = new Population();
        int populationIndex = 0;
        int failedTries = 0;
        final int MAX_TRIES = 15;
        // populate until the limit is reached
        while (populationIndex < GenerationSettings.getInstance().getPopulationSize()) {

            try {
                // perform 'natural' selection to determine the parents
                Chromosome father = performSelection();
                Chromosome mother = performSelection();

                Chromosome[] children;

                // perform the crossover operation if the crossover probability is met
                if (RandomService.RANDOM_GENERATOR.nextFloat() <= GenerationSettings.getInstance().getCrossoverProbability()) {
                    // perform crossover operation
                    children = performCrossover(father, mother);
                } else {
                    // if crossover probability is not met put father and mother to new population
                    children = new Chromosome[]{father, mother};
                }

                // perform mutation
                for (Chromosome child : children) {
                    performMutation(child);
                }

                // add children to new population
                for (Chromosome child : children) {
                    newPopulation.addChromosome(child);
                    populationIndex++;
                }

                failedTries = 0;

            } catch (Exception ex) {
                failedTries++;

                Logger.error("crossover failed", ex.getMessage());

                if (failedTries >= MAX_TRIES) {
                    throw new Exception("Population cannot be generated in a valid way.");
                }
            }

        }

        // when finished calculate fitness values
        newPopulation.calculateTotalFitness();

        return newPopulation;
    }

    /**
     * performs the 'natural' selection
     *
     * @return selected chromosome
     * @throws Exception if no chromosome could be found while selecting
     */
    public Chromosome performSelection() throws Exception {
        // generates random number between 0 and 1
        float randomPick = RandomService.RANDOM_GENERATOR.nextFloat();
        float sum = 0;
        List<Chromosome> chromosomes = getPopulation().getChromosomes();
        // iterates through population
        for (int index = 0; index < chromosomes.size(); index++) {

            // determines if the random number is in the range of the current chromosome
            if (randomPick >= sum && randomPick <= (sum += getSingleProbability(chromosomes.get(index)))) {

                // if the random number is in the range, then select this chromosome 
                return chromosomes.get(index);
            }
        }

        throw new Exception("no population item found with pick " + randomPick);
    }

    /**
     *
     * @param chromosome chromosome, of which the relative fitness should be
     * calculated
     * @return single probability of selection
     */
    public float getSingleProbability(Chromosome chromosome) {
        return getPopulation().getRelativeFitness(chromosome, true);
    }

    /**
     * performs the crossover operation
     *
     * @param father chromosome for mating
     * @param mother chromosome for mating
     * @return 2 children
     */
    public static Chromosome[] performCrossover(Chromosome father, Chromosome mother) throws Exception {

        // extract genes out of chromosomes
        List<IGene> fatherGenes = father.getGeneList().getGenes();
        List<IGene> motherGenes = mother.getGeneList().getGenes();

        Chromosome child1 = null;
        Chromosome child2 = null;
        boolean creationIsUnique;
        final int MAX_TRIES = 100;
        int failedTries = 0;

        // mate them until both of the chromosomes are valid or until the maximum of tries has been reached
        do {
            failedTries++;

            creationIsUnique = true;
            // generate the cutting index
            int divideAt = RandomService.getRandomNumberInRange(0, fatherGenes.size() - 1);

            // divides the father's first part off the rest, e.g. '1001' => '10|11' => '10'
            List firstPart = fatherGenes.subList(0, divideAt);
            // divides the father's second part off the rest, e.g. '1001' => '10|11' => '11'
            List secondPart = fatherGenes.subList(divideAt, fatherGenes.size());
            // divides the mother's first part off the rest, e.g. '1001' => '10|11' => '10'
            List thirdPart = motherGenes.subList(0, divideAt);
            // divides the mother's first part off the rest, e.g. '1001' => '10|11' => '11'
            List fourthPart = motherGenes.subList(divideAt, motherGenes.size());

            // combine parts to new gene lists
            List child1Genes = new ArrayList();
            child1Genes.addAll(firstPart);
            child1Genes.addAll(fourthPart);
            List child2Genes = new ArrayList();
            child2Genes.addAll(thirdPart);
            child2Genes.addAll(secondPart);

            try {
                // build chromosomes out of gene lists
                child1 = buildChromosome(child1Genes);
                child2 = buildChromosome(child2Genes);
            } catch (Exception ex) {
                if (failedTries >= MAX_TRIES) {
                    throw new Exception("Chromosomes cannot be crossed in a valid way.");
                }
                creationIsUnique = false;
                Logger.error("chromosome redundant", ex.getMessage());
            }
        } while (!creationIsUnique);

        // returns the two children after adding the parts together
        return new Chromosome[]{child1, child2};
    }

    /**
     *
     * @param geneList list of genes for building a chromosome
     * @return built chromosome
     * @throws Exception if the geneList contains redundant meals
     */
    public static Chromosome buildChromosome(List geneList) throws Exception {

        // build structure fo the chromosome
        GeneList<GeneList<Meal>> allMeals = new GeneList<GeneList<Meal>>();

        int courseIndex = 0;
        // holds existing courses to check redundancy
        ArrayList<Course[]> existingCourses = new ArrayList();

        while (courseIndex < geneList.size()) {

            GeneList<Meal> mealsPerDay = new GeneList<Meal>();

            for (MealType type : MealType.getMealTypesPerDay()) {

                Course[] courses;

                // new meal container
                courses = new Course[type.getNumberOfCourses()];

                for (int index = 0; index < courses.length; index++) {

                    // new meal
                    courses[index] = (Course) ((IGene) geneList.get(courseIndex));
                    courseIndex++;
                }

                if (checkRedundancy(courses, existingCourses)) {
                    throw new Exception("Chromosome is not valid; it contains redundant meals.");
                }

                // add meal to existing ones
                existingCourses.add(courses);

                mealsPerDay.add(new Meal(type, courses));


            }
            allMeals.add(mealsPerDay);
        }

        return new Chromosome(allMeals);
    }

    /**
     * performs the mutation of each gene if the mutation probability is met
     *
     * @param chromosome chromosome, which should be mutated
     * @return
     */
    public static void performMutation(Chromosome chromosome) throws Exception {

        int geneNumber = 0;
        // iterate through genes of 
        for (IGene gene : (List<IGene>) chromosome.getGeneList().getGenes()) {
            geneNumber++;

            // perform mutation if the mutation probability is met
            if (RandomService.RANDOM_GENERATOR.nextFloat() <= GenerationSettings.getInstance().getMutationProbability()) {

                Logger.log("MUTATION in gene", "#" + geneNumber + " " + gene.toString());
                Logger.log("MUTATION: old fitness", gene.getFitnessValue());

                // replace gene with new one
                gene = gene.getNewGene(geneNumber);

                Logger.log("MUTATION: new gene", gene.toString());
                Logger.log("MUTATION: new fitness", gene.getFitnessValue());

            }

        }

        if (checkRedundancy(chromosome)) {
            throw new Exception("Chromosome is not valid; it contains redundant meals.");
        }

    }

    public static Course[] getRedundancy(Course[] meal, List<Course[]> existingCoursesList) {

        int equalCourses;
        // iterating through existing meals
        for (Course[] existingMeal : existingCoursesList) {

            equalCourses = 0;
            // iterate through courses of this meal
            for (Course newCourse : meal) {
                // iterating through courses of a existing Meal
                for (Course existingCourse : existingMeal) {

                    if (newCourse == existingCourse) {
                        equalCourses++;
                        break;
                    }

                }
            }

            // the courses which form a meal are redundant if the whole meal 
            if (equalCourses == meal.length) {
                return existingMeal;
            }
        }
        return null;
    }

    /**
     * checks if the meal is redundant, returns true if so
     *
     * @param meal new courses forming a meal
     * @param existingCoursesList existing courses
     * @return boolean true if redundancy is met, false otherwise
     */
    public static boolean checkRedundancy(Course[] meal, List<Course[]> existingCoursesList) {

        // the courses which form a meal are redundant if the whole meal 
        if (getRedundancy(meal, existingCoursesList) != null) {
            return true;
        }
        return false;
    }

    /**
     * checks if the chromosome holds redundant meals
     *
     * @param chromosome chromosome to be checked
     * @return boolean true if redundancy is met, false otherwise
     */
    public static boolean checkRedundancy(Chromosome chromosome) {

        ArrayList<Course[]> existingCourses = new ArrayList<Course[]>();

        for (Object mealsPerOneDay : chromosome.getGeneList()) {
            for (Object meal : ((GeneList) mealsPerOneDay)) {

                if (checkRedundancy(((Meal) meal).getCourses(), existingCourses)) {

                    return true;
                }

            }

        }

        return false;
    }

    public static void substitueWithRedundancy(Chromosome chromosome) {

        for (Object mealsPerOneDay : chromosome.getGeneList()) {
            for (Object mealObject : ((GeneList) mealsPerOneDay)) {

                Meal meal = (Meal) mealObject;
                Course[] redundantCourses;
                if (null != (redundantCourses = getRedundancy(meal.getCourses(), Course.allMealCourses))) {

                    meal.setCourses(redundantCourses);

                    boolean mealExists = false;
                    for (Meal existingMeal : Meal.allMeals) {
                        if (meal.getType().equals(existingMeal.getType()) && meal.getCourses().equals(existingMeal.getCourses())) {
                            
                            ((GeneList) mealsPerOneDay).set(((GeneList) mealsPerOneDay).indexOf(meal), existingMeal);
                            meal = existingMeal;
                            mealExists = true;
                            break;
                        }
                    }
                    if (!mealExists) {
                        Meal.allMeals.add(meal);
                    }

                } else {
                    Course.allMealCourses.add(meal.getCourses());
                    Meal.allMeals.add(meal);
                }

            }

        }
    }

    /**
     * @return the population
     */
    public Population getPopulation() {
        return population;
    }

    /**
     * @param population the population to set
     */
    public void setPopulation(Population population) {
        this.population = population;
    }

    /**
     * @return the result
     */
    public static Result getResult() {
        return result;
    }

    /**
     * @param aResult the result to set
     */
    public static void setResult(Result aResult) {
        result = aResult;
    }

    /**
     * @return
     */
    public static boolean isStopEvolution() {
        return stopEvolution;
    }

    /**
     * @param aStopEvolution
     */
    public static void setStopEvolution(boolean aStopEvolution) {
        if (aStopEvolution == false) {
            Logger.fatal("Evolution process stopped", "");
        }
        stopEvolution = aStopEvolution;
    }
    
    /**
     * reset the index of the last generation which is memory optimized
     */
    public static void resetMemoryOptimizedGeneration() {
        memoryOptimizedGeneration = 0;
    }

    /**
     * 
     * @return user defined desired costs
     */
    public static float getDesiredCosts() {
        return desiredCosts;
    }

    /**
     * 
     * @param desiredCosts 
     */
    public static void setDesiredCosts(float desiredCosts) {
        Evolution.desiredCosts = desiredCosts;
    }
    
    /**
     * substitutes an existing generation with an empty one (setting to save memory)
     * @param evolutionIndex index of generation to substitute
     */
    public static void createEmptyEvolution(int evolutionIndex) {
        new Evolution(evolutionIndex);
    }
}
