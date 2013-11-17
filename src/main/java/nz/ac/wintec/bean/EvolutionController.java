/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import nz.ac.wintec.genetic.Chromosome;
import nz.ac.wintec.genetic.Evolution;
import nz.ac.wintec.genetic.GenerationSettings;
import nz.ac.wintec.genetic.Result;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.primefaces.model.chart.PieChartModel;

/**
 *
 * @author mike
 */
@ManagedBean(name = "evolutionController")
@SessionScoped
public class EvolutionController implements Serializable {

    private PieChartModel fitnessPieModel;
    private CartesianChartModel resultModel;
    private int numberActiveGeneration = 1;
    private int numberActiveChromosome = 1;
    private float evolutionProgress = 0;
    private float timeProgress = 0;
    private float freeMemoryProgress = 0;

    /**
     * 
     */
    public EvolutionController() {
        try {
            new Evolution();

            resetChartModels();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 
     * @return 
     */
    public String getChromosomeString() {
        try {
            return ((Chromosome) this.getGeneration().getPopulation().getChromosomes().get(0)).getChromosomeString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * 
     * @return 
     */
    public Evolution getGeneration() {
        return Evolution.getCurrentGeneration();
    }

    /**
     * 
     * @return 
     */
    public ArrayList<Evolution> getAllGenerations() {
        return Evolution.getAllGenerations();
    }

    /**
     * 
     */
    public void resetGenerations() {
        Evolution.resetGenerations();
        setEvolutionProgress(0);
        setTimeProgress(0);
        
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "generations resetted",
                    "the generations are removed and a initial one was generated");

        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * 
     * @return 
     */
    public int getNumberOfGenerations() {
        return Evolution.getNumberOfGenerations();
    }

    /**
     * 
     */
    public void createNewGeneration() {
        Evolution.setStopEvolution(false);
        
        this.getGeneration().createNewGeneration();
        resetChartModels();
        
        setEvolutionProgress((this.getNumberOfGenerations()*1f) / GenerationSettings.getInstance().getGenerationsNumber());
        
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "generation added",
                    "generation #"+this.getNumberOfGenerations()+" has been built");

        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * 
     */
    public void runEvolution() {
        this.getGeneration().runEvolution();
        resetChartModels();
        
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "evolution complete",
                    "the generation of chromosomes is complete");

        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    /**
     * 
     */
    public void stopEvolution() {
        Evolution.setStopEvolution(true);
    }

    /**
     * 
     * @return 
     */
    public PieChartModel getFitnessPieModel() {
        return fitnessPieModel;
    }

    /**
     * 
     */
    public void resetChartModels() {
        createFitnessPieModel();
        createResultModel();
    }

    /**
     * creates primeface pie model
     * @see http://www.primefaces.org/showcase/ui/pieChart.jsf
     */
    private void createFitnessPieModel() {
        this.fitnessPieModel = new PieChartModel();

        try {
            int index = 0;
            
            for (Chromosome chromosome : (List<Chromosome>) getActiveGeneration().getPopulation().getChromosomes()) {
                index++;
                
                getFitnessPieModel().set("#" + index, getActiveGeneration().getSingleProbability(chromosome));
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * enables to select a slice of the pie for further info
     * @param event 
     */
    public void fitnessPieItemSelect(ItemSelectEvent event) {
        try {
            Chromosome chromosome = (Chromosome) getActiveGeneration().getPopulation().getChromosomes().get(event.getItemIndex());
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "gene selected",
                    "chromosome #" + (event.getItemIndex() + 1) + " " + chromosome.toString() + " with costs of " + chromosome.getTotalFitness());

            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * creates primeface line chart model
     * containing best solutions and average solutions
     * @see http://www.primefaces.org/showcase/ui/lineChart.jsf
     */
    private void createResultModel() {
        resultModel = new CartesianChartModel();

        LineChartSeries bestSolutionSeries = new LineChartSeries();
        bestSolutionSeries.setMarkerStyle("diamond");
        bestSolutionSeries.setLabel("lowest cost in population");

        try {
            int index = 0;

            for (Evolution generation : getAllGenerations()) {
                index++;

                bestSolutionSeries.set(index, generation.getPopulation().getRelativeBestSolution().getTotalFitness());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        LineChartSeries averageSolutionSeries = new LineChartSeries();
        averageSolutionSeries.setMarkerStyle("diamond");
        averageSolutionSeries.setLabel("average costs in population");

        try {
            int index = 0;

            for (Evolution generation : getAllGenerations()) {
                index++;

                averageSolutionSeries.set(index, generation.getPopulation().getAverageFitness());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        getResultModel().addSeries(averageSolutionSeries);
        getResultModel().addSeries(bestSolutionSeries);
    }

    /**
     * 
     * @param chromosome 
     */
    public void setActiveChromosome(Chromosome chromosome) {
        if (getActiveGeneration() != null && getActiveGeneration().getPopulation() != null && getActiveGeneration().getPopulation().getChromosomes() != null) {
            this.setNumberActiveChromosome(getActiveGeneration().getPopulation().getChromosomes().indexOf(chromosome) + 1);
        }
    }

    /**
     * 
     * @return 
     */
    public Chromosome getActiveChromosome() {
        try {
            return (Chromosome) this.getActiveGeneration().getPopulation().getChromosomes().get(numberActiveChromosome - 1);
        } catch (Exception ex) {
            return null;
        }

    }

    /**
     * 
     * @param generation 
     */
    public void setActiveGeneration(Evolution generation) {
        if (!getAllGenerations().isEmpty()) {
            this.setNumberActiveGeneration(getAllGenerations().indexOf(generation) + 1);
        }
    }

    /**
     * 
     * @return 
     */
    public Evolution getActiveGeneration() {
        try {
            return this.getAllGenerations().get(numberActiveGeneration - 1);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * @return the bestSolutionModel
     */
    public CartesianChartModel getResultModel() {
        return resultModel;
    }

    /**
     * @return the numberActiveChromosome
     */
    public int getNumberActiveChromosome() {
        return numberActiveChromosome;
    }

    /**
     * @param numberActiveChromosome the numberActiveChromosome to set
     */
    public void setNumberActiveChromosome(int numberActiveChromosome) {
        if (numberActiveChromosome > getActiveGeneration().getPopulation().getChromosomes().size()) {
            numberActiveChromosome = getActiveGeneration().getPopulation().getChromosomes().size();
        }
        if (numberActiveChromosome < 1) {
            numberActiveChromosome = 1;
        }
        this.numberActiveChromosome = numberActiveChromosome;
    }

    /**
     * @return the numberActiveGeneration
     */
    public int getNumberActiveGeneration() {
        return numberActiveGeneration;
    }

    /**
     * @param numberActiveGeneration the numberActiveGeneration to set
     */
    public void setNumberActiveGeneration(int numberActiveGeneration) {
        if (numberActiveGeneration > getNumberOfGenerations()) {
            numberActiveGeneration = getNumberOfGenerations();
        }
        if (numberActiveGeneration < 1) {
            numberActiveGeneration = 1;
        }
        this.numberActiveGeneration = numberActiveGeneration;
        
        createFitnessPieModel();
    }
    
    /**
     * 
     * @return 
     */
    public Result getResult() {
        return Evolution.getResult();
    }

    /**
     * @return the evolutionProgress
     */
    public float getEvolutionProgress() {
        return evolutionProgress;
    }

    /**
     * @param evolutionProgress the evolutionProgress to set (between 0 and 1)
     */
    public void setEvolutionProgress(float evolutionProgress) {
        this.evolutionProgress = evolutionProgress * 100;
    }

    /**
     * @return the evolutionProgress
     */
    public float getTimeProgress() {
        return timeProgress;
    }

    /**
     * @param evolutionProgress the evolutionProgress to set (between 0 and 1)
     */
    public void setTimeProgress(float timeProgress) {
        this.timeProgress = timeProgress * 100;
    }

    /**
     * 
     * @return the progress of freeing memory
     */
    public float getFreeMemoryProgress() {
        return freeMemoryProgress;
    }

    /**
     * 
     * @param freeMemoryProgress the free memory progress to set (between 0 and 1)
     */
    public void setFreeMemoryProgress(float freeMemoryProgress) {
        this.freeMemoryProgress = freeMemoryProgress * 100;
    }
    
    /**
     * 
     * @return user defined desired costs
     */
    public float getDesiredCosts() {
        return Evolution.getDesiredCosts();
    }
    
    /**
     * 
     * @param desiredCosts 
     */
    public void setDesiredCosts(float desiredCosts) {
        Evolution.setDesiredCosts(desiredCosts);
    }
    
    
}
