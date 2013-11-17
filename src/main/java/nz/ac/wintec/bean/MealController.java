/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import nz.ac.wintec.genetic.GeneList;
import nz.ac.wintec.genetic.IGene;
import nz.ac.wintec.entity.Course;
import nz.ac.wintec.entity.Ingredient;
import nz.ac.wintec.entity.Meal;
import nz.ac.wintec.entity.MealType;
import nz.ac.wintec.entity.Restriction;
import nz.ac.wintec.genetic.GenerationSettings;
import nz.ac.wintec.genetic.IChromosomeGenerator;
import nz.ac.wintec.service.PersistenceService;
import nz.ac.wintec.service.RandomService;

/**
 *
 * @author mike
 */
@SessionScoped
@ManagedBean(name = "mealController")
public class MealController implements Serializable, IChromosomeGenerator {

    private ArrayList<MealType> mealTypesPerDay = new ArrayList<MealType>();
    private GeneList<GeneList<Meal>> desiredMeals;

    public MealController() {
        this.mealTypesPerDay.add(MealType.getBreakfast());
        this.mealTypesPerDay.add(MealType.getLunch());
        this.mealTypesPerDay.add(MealType.getDinner());
        
        resetDesiredMeals();
    }

    public GeneList<Meal> prepareMealsForOneDay(ArrayList<Course[]> existingCoursesList) throws Exception {

        GeneList<Meal> mealsPerDay = new GeneList<Meal>();

        for (MealType type : getMealTypesPerDay()) {

            Course[] courses;
            int count = 0;
            final int MAX_TRIES = 10000;
            // loop until the meal is unique
            do {

                courses = new Course[type.getNumberOfCourses()];

                for (int index = 0; index < courses.length; index++) {

                    // get all restrictions of the current meal type
                    List<Restriction> restrictions = PersistenceService.getManagedBeanInstance(RestrictionController.class).getAllItemsOfTypeAndOrder(type, index + 1);

                    // get a random item
                    courses[index] = RandomService.getRandomItem(restrictions).getCourse();

                }

                if (++count == MAX_TRIES) {
                    throw new Exception("No way of generating any further unique meals found.");
                }

            } while (checkRedundancy(courses, existingCoursesList));

            existingCoursesList.add(courses);
            mealsPerDay.add(new Meal(type, courses));

        }

        return mealsPerDay;
    }

    /**
     * checks if the meal is redundant, returns true if so
     *
     * @param courses
     * @param mealsPerDay
     * @param allMeals
     * @return boolean
     */
    public boolean checkRedundancy(Course[] courses, ArrayList<Course[]> existingCoursesList) {

        
        int equalCourses;
        for (Course[] existingCourses : existingCoursesList) {
            
            equalCourses = 0;
            for(Course newCourse : courses) {
                for(Course existingCourse : existingCourses) {
                    
                    if(newCourse == existingCourse) {
                        equalCourses++;
                        break;
                    }
                    
                }
            }

            if (equalCourses == courses.length) {
                return true;
            }
        }

        return false;
    }

    public GeneList<GeneList<Meal>> prepareMealsForMultipleDays(int numberOfDays) {

        ArrayList<Course[]> existingCoursesList = new ArrayList<Course[]>();
        GeneList<GeneList<Meal>> allMeals = new GeneList<GeneList<Meal>>();

        try {
            for (int index = 0; index < numberOfDays; index++) {

                GeneList<Meal> mealsForOneDay = prepareMealsForOneDay(existingCoursesList);
                if (mealsForOneDay.size() > 0) {
                    allMeals.add(mealsForOneDay);
                }
            }

            if (allMeals.isEmpty() && numberOfDays > 0) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "There is no data available to generate any meals"));
            }
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", ex.getMessage()));
            Logger.getLogger(MealController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return allMeals;
    }

    /**
     * @return the mealTypesPerDay
     */
    public ArrayList<MealType> getMealTypesPerDay() {
        return mealTypesPerDay;
    }

    /**
     * @param mealTypesPerDay the mealTypesPerDay to set
     */
    public void setMealTypesPerDay(ArrayList<MealType> mealTypesPerDay) {
        this.mealTypesPerDay = mealTypesPerDay;
    }

    /**
     * @return the desiredMeals
     */
    public GeneList<GeneList<Meal>> getDesiredMeals() {
        return desiredMeals;
    }
    
    public void generateMeals() {
        resetDesiredMeals();
        cleanInvalidData();
        desiredMeals = prepareMealsForMultipleDays(GenerationSettings.getInstance().getNumberOfDays());
    
    }
    
    private void cleanInvalidData() {
        
        // remove all ingredients not containing any costs
        List<Ingredient> allIngredients = PersistenceService.getManagedBeanInstance(IngredientController.class).getItems();
        for (int index = 0; index < allIngredients.size(); index++) {
            if (allIngredients.get(index).getCost() == null) {
                allIngredients.remove(index);
            }
        }
        // remove all courses with ingredients not containing any costs
        List<Course> allCourses = PersistenceService.getManagedBeanInstance(CourseController.class).getItems();
        for (int index = 0; index < allCourses.size(); index++) {
            for (Ingredient ingredient : allCourses.get(index).getIngredients()) {
                if(!allIngredients.contains(ingredient)) {
                    allCourses.remove(index);
                }
            }
        }

        // remove all ingredients not containing any costs
        List<Restriction> allRestrictions = PersistenceService.getManagedBeanInstance(RestrictionController.class).getItems();
        for (int index = 0; index < allRestrictions.size(); index++) {
            if(!allCourses.contains(allRestrictions.get(index).getCourse())) {
                allRestrictions.remove(index);
            }
        }
    }

    /**
     * @param desiredMeals the desiredMeals to set
     */
    public void setDesiredMeals(GeneList<GeneList<Meal>> desiredMeals) {
        this.desiredMeals = desiredMeals;
    }

    public void resetDesiredMeals() {
        this.desiredMeals = new GeneList<GeneList<Meal>>();
    }
    
    public List<IGene> getChromosome() {
        return desiredMeals.getGenes();
    }

    @Override
    public GeneList getNewChromosome() {
        return prepareMealsForMultipleDays(GenerationSettings.getInstance().getNumberOfDays());
    }
}
