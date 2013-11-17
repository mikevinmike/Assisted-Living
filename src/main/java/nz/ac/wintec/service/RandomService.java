/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.service;

import java.util.List;
import java.util.Random;

/**
 * class, which deals with randomness
 * @author mike
 */
public class RandomService {
    
    // generator used for creating random values
    public final static Random RANDOM_GENERATOR = new Random();
    
    /**
     * 
     * @param <T> class of items
     * @param allItems list of items to choose from
     * @return a random item from the list
     * @throws Exception if there are not items in the list
     */
    public static <T> T getRandomItem(List<T> allItems) throws Exception {
        if(allItems.isEmpty()) {
            throw new Exception("There are no items to choose.");
        }
        return allItems.get( getRandomNumberInRange(0, allItems.size()-1) );
    }
    
    /**
     * 
     * @param min minimum
     * @param max maximum
     * @return random number between minimum and maximum
     * @author Peter Mortensen
     * @see http://stackoverflow.com/questions/363681/generating-random-numbers-in-a-range-with-java
     */
    public static int getRandomNumberInRange(int min, int max) {
        return min + (int)(RANDOM_GENERATOR.nextFloat() * ((max - min) + 1));
    }
}
