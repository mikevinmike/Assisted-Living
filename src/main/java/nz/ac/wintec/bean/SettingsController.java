/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.bean;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import nz.ac.wintec.entity.Course;
import nz.ac.wintec.entity.Meal;
import nz.ac.wintec.genetic.GenerationSettings;
import nz.ac.wintec.service.PersistenceService;

/**
 * singleton for settings
 * @author mike
 */
@ManagedBean
@ApplicationScoped
public class SettingsController {
    
    // generation settings, which is a singleton
    private final GenerationSettings model;
    
    public SettingsController() {
        this.model = GenerationSettings.getInstance();
    }
    
    
    /**
     * @return the numberOfWeeks
     */
    public int getNumberOfWeeks() {
        return this.model.getNumberOfWeeks();
    }

    /**
     * @param numberOfWeeks the numberOfWeeks to set
     */
    public void setNumberOfWeeks(int numberOfWeeks) {
        this.model.setNumberOfWeeks(numberOfWeeks);
    }
  
    
    /**
     * @return the geneClass
     */
    public String getGeneClass() {
        return this.model.getGeneClass().getSimpleName().toString();
    }

    /**
     * @param geneClass the geneClass to set
     */
    private void setGeneClass(Class geneClass) {
        this.model.setGeneClass(geneClass);
        
        PersistenceService.getManagedBeanInstance(EvolutionController.class).resetChartModels();
    }
    
    /**
     * @param geneClass the geneClass to set
     */
    public void setGeneClass(String geneClass) {
        if(geneClass.equals("Meal")) {
            this.setGeneClass(Meal.class);
        } else if(geneClass.equals("Course")) {
            this.setGeneClass(Course.class);
        }
    }
    
    /**
     * 
     */
    public void save() {        
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Succesful", "Settings have been saved");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    
    /**
     * @return the crossoverProbability
     */
    public float getCrossoverProbability() {
        return this.model.getCrossoverProbability();
    }

    /**
     * @param crossoverProbability the crossoverProbability to set
     */
    public void setCrossoverProbability(float crossoverProbability) {
        this.model.setCrossoverProbability(crossoverProbability);
    }

    /**
     * @return the mutationProbability
     */
    public float getMutationProbability() {
        return this.model.getMutationProbability();
    }

    /**
     * @param mutationProbability the mutationProbability to set
     */
    public void setMutationProbability(float mutationProbability) {
        this.model.setMutationProbability(mutationProbability);
    }

    /**
     * @return the generationsNumber
     */
    public int getGenerationsNumber() {
        return this.model.getGenerationsNumber();
    }

    /**
     * @param generationsNumber the generationsNumber to set
     */
    public void setGenerationsNumber(int generationsNumber) {
        this.model.setGenerationsNumber(generationsNumber);
    }

    /**
     * @return the populationSize
     */
    public int getPopulationSize() {
        return this.model.getPopulationSize();
    }

    /**
     * @param populationSize the populationSize to set
     */
    public void setPopulationSize(int populationSize) {
        this.model.setPopulationSize(populationSize);
    }

    /**
     * @return the maxMinutes
     */
    public float getMaxMinutes() {
        return this.model.getMaxMinutes();
    }

    /**
     * @param maxMinutes the maxMinutes to set
     */
    public void setMaxMinutes(float maxMinutes) {
        this.model.setMaxMinutes(maxMinutes);
    }

    /**
     * 
     * @return if old generations should be dismissed
     */
    public boolean isDismissOldGenerations() {
        return this.model.isDismissOldGenerations();
    }

    /**
     * 
     * @param dismissOldGenerations if old generations should be dismissed
     */
    public void setDismissOldGenerations(boolean dismissOldGenerations) {
        this.model.setDismissOldGenerations(dismissOldGenerations);
    }
    
}
