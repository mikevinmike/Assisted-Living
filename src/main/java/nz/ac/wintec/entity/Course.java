/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import nz.ac.wintec.bean.RestrictionController;
import nz.ac.wintec.genetic.Evolution;
import nz.ac.wintec.genetic.IGene;
import nz.ac.wintec.service.PersistenceService;
import nz.ac.wintec.service.RandomService;

/**
 *
 * @author mike
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Course implements Serializable, IGene {

    private static final long serialVersionUID = 1L;
    @XmlElement
    private Long id;
    @XmlElement
    private String name;
    @XmlElement
    private List<Ingredient> ingredients = new ArrayList<Ingredient>();
    @XmlElement
    private List<Float> quantities = new ArrayList<Float>();

    @XmlTransient
    public static ArrayList<Course[]> allMealCourses = new ArrayList<Course[]>();
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Course)) {
            return false;
        }
        Course other = (Course) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        //return "nz.ac.wintec.smartshopper.Ingredient[ id=" + id + " ]";
        return this.getName();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the quantity
     */
    public List<Float> getQuantities() {
        return quantities;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantities(List<Float> quantities) {
        this.quantities = quantities;
    }

    /**
     * @return the ingredient
     */
    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public String getIngredientString() {
        String output = "";
        for (Ingredient ingredient : this.getIngredients()) {
            output += (output.isEmpty() ? "" : ", ") + ingredient.getName();
        }
        return output;
    }

    public String getQuantitiesString() {
        String output = "";
        for (Float quantity : this.getQuantities()) {
            output += (output.isEmpty() ? "" : ", ") + quantity;
        }
        return output;
    }

    public void addIngredient(Ingredient ingredient, Float quantity) {
        for (int index = 0; index < getIngredients().size(); index++) {
            if (getIngredients().get(index).getName().toLowerCase().equals(ingredient.getName().toLowerCase())) {
                getIngredients().set(index, ingredient);
                getQuantities().set(index, quantity);
                return;
            }
        }

        getIngredients().add(ingredient);
        getQuantities().add(quantity);
    }

    @Override
    public float getFitnessValue() {
        float costs = 0;
        for (int index = 0; index < this.getIngredients().size(); index++) {
            costs += this.getIngredients().get(index).getCost() * this.getQuantities().get(index);
        }
        return costs;
    }

    @Override
    public IGene getClonedGene() {
        try {
            return (IGene) this.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(Course.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public IGene getNewGene(int geneNumber) throws Exception {

        int number = 0;

        while (number < geneNumber) {
            for (MealType type : MealType.getMealTypesPerDay()) {

                int order = 1;
                for (int mealTypeIndex = 0; mealTypeIndex < type.getNumberOfCourses(); mealTypeIndex++) {
                    number++;

            if (number == geneNumber) {
                        List<Restriction> restrictions = PersistenceService.getManagedBeanInstance(RestrictionController.class).getAllItemsOfTypeAndOrder(type, order);

                        return RandomService.getRandomItem(restrictions).getCourse();
                    }

                    order++;
                }
            }
        }

        throw new Exception("No substitution gene found.");
    }
}
