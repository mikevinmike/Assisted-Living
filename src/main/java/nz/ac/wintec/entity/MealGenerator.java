/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import nz.ac.wintec.bean.CourseController;
import nz.ac.wintec.bean.IngredientController;
import nz.ac.wintec.bean.MealController;
import nz.ac.wintec.bean.RestrictionController;
import nz.ac.wintec.genetic.Evolution;
import nz.ac.wintec.genetic.GeneList;
import nz.ac.wintec.genetic.GenerationSettings;
import nz.ac.wintec.genetic.IChromosomeGenerator;
import nz.ac.wintec.service.PersistenceService;
import nz.ac.wintec.service.RandomService;

/**
 * generates meals
 *
 * @author mike
 */
public class MealGenerator implements IChromosomeGenerator {

    private static MealGenerator instance;

    private MealGenerator() {
        cleanInvalidData();
    }

    /**
     *
     * @return single instance of IChromosomeGenerator
     */
    public static IChromosomeGenerator getInstance() {
        if (instance == null) {
            instance = new MealGenerator();
        }
        return instance;
    }

    /**
     * clean import data
     */
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
                if (!allIngredients.contains(ingredient)) {
                    allCourses.remove(index);
                }
            }
        }

        // remove all ingredients not containing any costs
        List<Restriction> allRestrictions = PersistenceService.getManagedBeanInstance(RestrictionController.class).getItems();
        for (int index = 0; index < allRestrictions.size(); index++) {
            if (!allCourses.contains(allRestrictions.get(index).getCourse())) {
                allRestrictions.remove(index);
            }
        }
    }

    @Override
    public GeneList getNewChromosome() {
        return prepareMealsForMultipleDays(GenerationSettings.getInstance().getNumberOfDays());
    }

    /**
     * generates meals for one day
     *
     * @param existingCoursesList list of meals already generated
     * @return list of meals for one day
     * @throws Exception if there is no way found to generate further unique
     * meals
     */
    public GeneList<Meal> prepareMealsForOneDay(ArrayList<Course[]> existingCoursesList) throws Exception {

        GeneList<Meal> mealsPerDay = new GeneList<Meal>();

        for (MealType type : MealType.getMealTypesPerDay()) {
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

            } while (Evolution.checkRedundancy(courses, existingCoursesList));

            existingCoursesList.add(courses);
            Course.allMealCourses.add(courses);
            
            Meal meal = new Meal(type, courses);
            Meal.allMeals.add(meal);
            
            mealsPerDay.add(meal);

        }

        return mealsPerDay;
    }

    /**
     * generates meals for the specified number of days
     *
     * @param numberOfDays number of days for wich the meals should be generated
     * @return chromosome / meal plan
     */
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
}
