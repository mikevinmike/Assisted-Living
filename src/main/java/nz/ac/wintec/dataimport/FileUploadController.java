/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.dataimport;

import nz.ac.wintec.service.PersistenceService;
import nz.ac.wintec.entity.Ingredient;
import au.com.bytecode.opencsv.CSVReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import nz.ac.wintec.bean.CourseController;
import nz.ac.wintec.bean.IngredientController;
import nz.ac.wintec.bean.RestrictionController;
import nz.ac.wintec.entity.Course;
import nz.ac.wintec.entity.Restriction;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author mike
 */
@ManagedBean(name = "fileUploadController")
@ViewScoped
public class FileUploadController implements Serializable {

    private final char CSV_SEPERATOR = ',';
    private final char CSV_QUOTECHAR = '"';
    private final String EMPTY_ERROR = "The csv file does not contain any entries";
    private final String CHARSET = "ISO-8859-1";
    private final int INGREDIENT_NAME_INDEX = 0;
    private final int INGREDIENT_QUANTITY_INDEX = 1;
    private final int COURSE_NAME_INDEX = 0;
    private final int COURSE_INGREDIENT_INDEX = 1;
    private final int COURSE_QUANTITY_INDEX = 2;
    private final int RESTRICTION_COURSE_INDEX = 0;
    private final int RESTRICTION_MEAL_INDEX = 1;
    private final int RESTRICTION_ORDER_INDEX = 2;
    private final int INGREDIENT_NUMBER_OF_COLUMNS = 2;
    private final int COURSE_NUMBER_OF_COLUMNS = 3;
    private final int RESTRICTION_NUMBER_OF_COLUMNS = 3;
    private final String KEYWORD_INGREDIENT = "ingredient";
    private final String KEYWORD_COURSE = "course";
    private final String KEYWORD_RESTRICTION = "restriction";

    /**
     * 
     * @param event 
     */
    public void handleIngredientsUpload(FileUploadEvent event) {

        try {
            handleFile(event.getFile());

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Succesful", event.getFile().getFileName() + " has been imported");
            FacesContext.getCurrentInstance().addMessage(null, msg);

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", event.getFile().getFileName() + " could not be imported");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }

    }

    /**
     * 
     * @param file
     * @throws Exception 
     */
    private void handleFile(UploadedFile file) throws Exception {
        if (file.getFileName().toLowerCase().contains(KEYWORD_RESTRICTION)) {
            importCsv(file.getInputstream(), KEYWORD_RESTRICTION);
        } else if (file.getFileName().toLowerCase().contains(KEYWORD_COURSE)) {
            importCsv(file.getInputstream(), KEYWORD_COURSE);
        } else if (file.getFileName().toLowerCase().contains(KEYWORD_INGREDIENT)) {
            importCsv(file.getInputstream(), KEYWORD_INGREDIENT);
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", file.getFileName() + " could not be mapped");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    /**
     * 
     * @param is
     * @param type
     * @throws Exception 
     */
    private void importCsv(InputStream is, String type) throws Exception {

        importCsv(new BufferedReader(new InputStreamReader(is, CHARSET)), type);

    }

    /**
     * 
     * @param br
     * @param type
     * @throws Exception 
     */
    private void importCsv(BufferedReader br, String type) throws Exception {

        CSVReader reader = new CSVReader(br, CSV_SEPERATOR, CSV_QUOTECHAR);
        String[] line = reader.readNext();

        if (line == null || line.length <= 0) {
            throw new Exception(EMPTY_ERROR);
        }

        IngredientController ingredientController = PersistenceService.getManagedBeanInstance(IngredientController.class);
        CourseController courseController = PersistenceService.getManagedBeanInstance(CourseController.class);
        RestrictionController restrictionController = PersistenceService.getManagedBeanInstance(RestrictionController.class);

        int lineIndex = 0;
        while ((line = reader.readNext()) != null) {
            lineIndex++;

            int numberOfColumns = 0;
            if (type.equals(KEYWORD_RESTRICTION)) {
                numberOfColumns = RESTRICTION_NUMBER_OF_COLUMNS;
            } else if (type.equals(KEYWORD_COURSE)) {
                numberOfColumns = COURSE_NUMBER_OF_COLUMNS;
            } else if (type.equals(KEYWORD_INGREDIENT)) {
                numberOfColumns = INGREDIENT_NUMBER_OF_COLUMNS;
            }


            if (line.length != numberOfColumns) {
                continue;
            }

            if (type.equals(KEYWORD_RESTRICTION)) {
                saveRestriction(line, restrictionController, courseController);
            } else if (type.equals(KEYWORD_COURSE)) {
                saveCourse(line, courseController, ingredientController);
            } else if (type.equals(KEYWORD_INGREDIENT)) {
                saveIngredient(line, ingredientController, courseController);
            }

        }


        if (lineIndex == 0) {
            throw new Exception(EMPTY_ERROR);
        }
    }

    /**
     * 
     * @param line
     * @param ingredientController
     * @param courseController 
     */
    private void saveIngredient(String[] line, IngredientController ingredientController, CourseController courseController) {

        boolean newEntry = true;
        Ingredient currentIngredient = ingredientController.prepareCreate(null);
        for (Ingredient ingredient : ingredientController.getItems()) {
            if (ingredient.getName().toLowerCase().equals(line[INGREDIENT_NAME_INDEX].trim().toLowerCase())) {
                currentIngredient = ingredient;
                newEntry = false;
                break;
            }
        }

        currentIngredient.setName(line[INGREDIENT_NAME_INDEX].trim());
        currentIngredient.setCost(Float.parseFloat(line[INGREDIENT_QUANTITY_INDEX].trim()));

        if (newEntry) {
            PersistenceService.saveNew(IngredientController.class, currentIngredient);
        } else {
            PersistenceService.save(IngredientController.class, currentIngredient);

            for (Course course : courseController.getItems()) {
                for (Ingredient ingredient : course.getIngredients()) {
                    if (currentIngredient.getName().equals(ingredient.getName())) {
                        ingredient = currentIngredient;
                        PersistenceService.save(CourseController.class, course);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 
     * @param line
     * @param courseController
     * @param ingredientController 
     */
    private void saveCourse(String[] line, CourseController courseController, IngredientController ingredientController) {

        boolean newEntry = true;
        Course currentCourse = courseController.prepareCreate(null);
        for (Course course : courseController.getItems()) {
            if (course.getName().toLowerCase().equals(line[COURSE_NAME_INDEX].trim().toLowerCase())) {
                currentCourse = course;
                newEntry = false;
                break;
            }
        }

        currentCourse.setName(line[COURSE_NAME_INDEX].trim());

        boolean newIngredient = true;
        Ingredient currentIngredient = ingredientController.prepareCreate(null);
        currentIngredient.setName(line[COURSE_INGREDIENT_INDEX].trim());
        for (Ingredient ingredient : ingredientController.getItems()) {
            if (ingredient.getName().toLowerCase().equals(line[COURSE_INGREDIENT_INDEX].trim().toLowerCase())) {
                currentIngredient = ingredient;
                newIngredient = false;
                break;
            }
        }
        if (newIngredient) {
            PersistenceService.saveNew(IngredientController.class, currentIngredient);
        } else {
            PersistenceService.save(IngredientController.class, currentIngredient);
        }

        currentCourse.addIngredient(currentIngredient, Float.parseFloat(line[COURSE_QUANTITY_INDEX].trim()));

        if (newEntry) {
            PersistenceService.saveNew(CourseController.class, currentCourse);
        } else {
            PersistenceService.save(CourseController.class, currentCourse);
        }
    }

    /**
     * 
     * @param line
     * @param restrictionController
     * @param courseController 
     */
    private void saveRestriction(String[] line, RestrictionController restrictionController, CourseController courseController) {

        boolean newEntry = true;
        Restriction currentRestriction = restrictionController.prepareCreate(null);
        for (Restriction restriction : restrictionController.getItems()) {
            if (restriction.getCourse().getName().toLowerCase().equals(line[RESTRICTION_COURSE_INDEX].trim().toLowerCase())
                    && restriction.getMeal().toLowerCase().equals(line[RESTRICTION_MEAL_INDEX].trim().toLowerCase())
                    && restriction.getOrder() == Integer.parseInt(line[RESTRICTION_ORDER_INDEX].trim())) {
                currentRestriction = restriction;
                newEntry = false;
                System.err.println("no new entry");
                break;
            }
        }

        currentRestriction.setMeal(line[RESTRICTION_MEAL_INDEX].trim());
        currentRestriction.setOrder(Integer.parseInt(line[RESTRICTION_ORDER_INDEX].trim()));

        boolean newCourse = true;
        Course currentCourse = new Course();
        currentCourse.setName(line[RESTRICTION_COURSE_INDEX].trim());
        for (Course course : courseController.getItems()) {
            if (course.getName().toLowerCase().equals(line[RESTRICTION_COURSE_INDEX].trim().toLowerCase())) {
                currentCourse = course;
                newCourse = false;
                break;
            }
        }
        if (newCourse) {
            // avoid corrupted restrictions
            return;
            //PersistenceService.saveNew(CourseController.class, currentCourse);
        } else {
            PersistenceService.save(CourseController.class, currentCourse);
        }
        currentRestriction.setCourse(currentCourse);

        if (newEntry) {
            PersistenceService.saveNew(RestrictionController.class, currentRestriction);
        } else {
            PersistenceService.save(RestrictionController.class, currentRestriction);
        }
    }

    /**
     * 
     */
    public void resetData() {
        PersistenceService.getManagedBeanInstance(RestrictionController.class).resetItems();
        PersistenceService.getManagedBeanInstance(CourseController.class).resetItems();
        PersistenceService.getManagedBeanInstance(IngredientController.class).resetItems();

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Succesful", "Data has been reset");
        FacesContext.getCurrentInstance().addMessage(null, msg);

    }
}
