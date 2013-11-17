/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * defines a meal type
 * @author mike
 */
public class MealType implements Serializable {
    
    private int numberOfCourses;
    private String name;
    private static List<MealType> mealTypesPerDay = new ArrayList();
    
    public MealType(String name, int numberOfCourses) {
        this.name = name;
        this.numberOfCourses = numberOfCourses;
    }
    
    /**
     * breakfast has 2 courses
     * @return meal type breakfast
     */
    public static MealType getBreakfast() {
        return new MealType("breakfast", 2);
    }
    
    /**
     * lunch has 3 courses
     * @return meal type lunch
     */
    public static MealType getLunch() {
        return new MealType("lunch", 3);
    }
    
    /**
     * dinner has 3 courses
     * @return meal type dinner
     */
    public static MealType getDinner() {
        return new MealType("dinner", 3);
    }
    
    public static List<MealType> getMealTypesPerDay() {
        if(mealTypesPerDay.isEmpty()) {
            mealTypesPerDay.add(getBreakfast());
            mealTypesPerDay.add(getLunch());
            mealTypesPerDay.add(getDinner());
        }
        
        return mealTypesPerDay;
    }
    
    /**
     * @return the numberOfCourses
     */
    public int getNumberOfCourses() {
        return numberOfCourses;
    }

    /**
     * @param numberOfCourses the numberOfCourses to set
     */
    public void setNumberOfCourses(int numberOfCourses) {
        this.numberOfCourses = numberOfCourses;
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
    
    @Override
    public String toString() {
        return this.getName();
    }
}
