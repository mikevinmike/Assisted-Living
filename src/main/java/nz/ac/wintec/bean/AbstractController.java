package nz.ac.wintec.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.event.ActionEvent;

import java.util.ResourceBundle;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import nz.ac.wintec.bean.util.JsfUtil;

/**
 * Represents an abstract shell of to be used as JSF Controller to be used in
 * AJAX-enabled applications. No outcomes will be generated from its methods
 * since handling is designed to be done inside one page.
 */
public abstract class AbstractController<T> {

    @XmlTransient
    private Class<T> itemClass;
    @XmlTransient
    private T selected;
    @XmlElement
    private List<T> items;
    
    @XmlTransient
    private static String resourceFolder = AbstractController.class.getResource("/"+AbstractController.class.getName().replace(".", "/").replace(AbstractController.class.getSimpleName(), "")).getFile();
    @XmlTransient
    private String file = "any.xml";
    

    private enum PersistAction {

        CREATE,
        DELETE,
        UPDATE
    }

    public AbstractController(Class<T> itemClass) {
        this.itemClass = itemClass;
        file = this.itemClass.getSimpleName()+".xml";
    }

    public T getSelected() {
        return selected;
    }

    public void setSelected(T selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
        // Nothing to do if entity does not have any embeddable key.
    }

    ;

    protected void initializeEmbeddableKey() {
        // Nothing to do if entity does not have any embeddable key.
    }

    /**
     * Returns all items in a List object
     *
     * @return
     */
    public List<T> getItems() {
        if (items == null) {
            items = new ArrayList<T>();
        }
        return items;
    }
    
    protected void setItems(List<T> items) {
        this.items = items;
    }
    
    public void resetItems() {
        this.setItems(new ArrayList<T>());
    }

    public void save(ActionEvent event) {
        String msg = ResourceBundle.getBundle("/Bundle").getString(itemClass.getSimpleName() + "Updated");
        this.marshall();
    }

    public void saveNew(ActionEvent event) {
        String msg = ResourceBundle.getBundle("/Bundle").getString(itemClass.getSimpleName() + "Created");
        this.getItems().add(selected);
        this.marshall();
    }

    public void delete(ActionEvent event) {
        String msg = ResourceBundle.getBundle("/Bundle").getString(itemClass.getSimpleName() + "Deleted");
        getItems().remove(selected);
        if (!isValidationFailed()) {
            selected = null; // Remove selection
        }
        this.marshall();
    }

    /**
     * Creates a new instance of an underlying entity and assigns it to Selected
     * property.
     *
     * @return
     */
    public T prepareCreate(ActionEvent event) {
        T newItem;
        try {
            newItem = itemClass.newInstance();
            this.selected = newItem;
            initializeEmbeddableKey();
            return newItem;
        } catch (InstantiationException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public boolean isValidationFailed() {
        return JsfUtil.isValidationFailed();
    }
    
    /**
     * save instance as xml file
     */
    protected void marshall() {
        try {
            JAXBContext context = getJaxbContext();
            
            Marshaller marshaller = context.createMarshaller();

            marshaller.marshal(this, new File(resourceFolder+file));

            System.out.println(this.getClass().getSimpleName()+" marhsalled");
        } catch (JAXBException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private JAXBContext getJaxbContext() throws JAXBException {
        return JAXBContext.newInstance( new Class[]{this.getClass(), this.itemClass} );
    }
    
    /**
     * get instance out of xml file
     * @return 
     */
    protected AbstractController<T> unmarshalConfig() {

        AbstractController entityController = null;

        try {
            JAXBContext context = getJaxbContext();

            Unmarshaller unmarshaller = context.createUnmarshaller();

            File entitiesFile;
            if(!(entitiesFile = new File(resourceFolder+file)).exists()) {
                
                marshall();                
            }
            entityController = (AbstractController) unmarshaller.unmarshal(entitiesFile);

        } catch (JAXBException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }

        return entityController;
    }
}