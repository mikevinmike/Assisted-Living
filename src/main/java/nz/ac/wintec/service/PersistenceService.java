/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.service;

import javax.faces.context.FacesContext;
import javax.faces.bean.ManagedBean;
import nz.ac.wintec.bean.AbstractController;

/**
 * class, which deals with JSF managed beans and provides a simple interface for persisting them
 * @author mike
 */
public class PersistenceService {

    /**
     * saves a new record
     * @param controllerClass controller class of the entity
     * @param entity entity to be saved
     */
    public static void saveNew(Class<? extends AbstractController> controllerClass, Object entity) {

        AbstractController controller = getManagedBeanInstance(controllerClass);
        controller.setSelected(entity);
        controller.saveNew(null);
    }
    
    /**
     * updates a record
     * @param controllerClass controller class of the entity
     * @param entity entity to be saved
     */
    public static void save(Class<? extends AbstractController> controllerClass, Object entity) {

        AbstractController controller = getManagedBeanInstance(controllerClass);
        controller.setSelected(entity);
        controller.save(null);
    }

    /**
     * 
     * @param <T> any managed bean class
     * @param managedBeanClass any managed bean class
     * @return instance of a managed bean class
     */
    public static <T> T getManagedBeanInstance(Class<T> managedBeanClass) {
        return FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(FacesContext.getCurrentInstance(), "#{"+managedBeanClass.getAnnotation(ManagedBean.class).name()+"}", managedBeanClass);
    }
}
