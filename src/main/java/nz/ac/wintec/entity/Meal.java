/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nz.ac.wintec.bean.RestrictionController;
import nz.ac.wintec.genetic.GenerationSettings;
import nz.ac.wintec.genetic.IGene;
import nz.ac.wintec.genetic.IGeneHolder;
import nz.ac.wintec.service.PersistenceService;
import nz.ac.wintec.service.RandomService;

/**
 *
 * @author mike
 */
public class Meal implements Serializable, IGeneHolder, IGene {

    private MealType type;
    private Course[] courses;
    
    public static ArrayList<Meal> allMeals = new ArrayList<Meal>();

    public Meal(MealType type, Course[] courses) throws Exception {
        if (courses.length != type.getNumberOfCourses()) {
            throw new Exception("number of courses provided do not match with the required ones; for " + type.getName() + " " + type.getNumberOfCourses() + " must be provided (" + courses.length + " provided)");
        }

        this.type = type;
        this.courses = courses;
    }

    @Override
    public IGene getClonedGene() {
        try {
            return (IGene) this.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(Meal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * @return the type
     */
    public MealType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(MealType type) {
        this.type = type;
    }

    /**
     * @return the courses
     */
    public Course[] getCourses() {
        return courses;
    }

    /**
     * @param courses the courses to set
     */
    public void setCourses(Course[] courses) {
        this.courses = courses;
    }

    @Override
    public List<IGene> getGenes() {
        if (!GenerationSettings.getInstance().getGeneClass().equals(this.getClass())) {
            List<IGene> courses = new ArrayList<IGene>();
            courses.addAll(Arrays.asList(getCourses()));

            return courses;
        } else {
            List<IGene> meals = new ArrayList<IGene>();
            meals.add(this);
            return meals;
        }
    }

    @Override
    public String toString() {
        String output = "";
        if (!GenerationSettings.getInstance().getGeneClass().equals(this.getClass())) {
            for (Course course : getCourses()) {
                output += (output.isEmpty() ? "" : " | ") + course.toString();
            }
        } else {
            for (Course course : getCourses()) {
                output += (output.isEmpty() ? "" : " , ") + course.toString();
            }
            output = getType() + "{ " + output + " }";
        }
        return output;
    }

    @Override
    public float getFitnessValue() {
        float costs = 0;
        for (Course course : getCourses()) {
            costs += course.getFitnessValue();
        }
        return costs;
    }

    @Override
    public IGene getNewGene(int geneNumber) throws Exception {

        Course[] courses = new Course[this.getType().getNumberOfCourses()];

        for (int index = 0; index < courses.length; index++) {

            // get all restrictions of the current meal type
            List<Restriction> restrictions = PersistenceService.getManagedBeanInstance(RestrictionController.class).getAllItemsOfTypeAndOrder(this.getType(), index + 1);

            // get a random item
            courses[index] = RandomService.getRandomItem(restrictions).getCourse();

        }

        return new Meal(getType(), courses);

    }
}
