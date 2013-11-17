package nz.ac.wintec.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.persistence.Persistence;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import nz.ac.wintec.entity.Course;
import nz.ac.wintec.entity.MealType;
import nz.ac.wintec.entity.Restriction;
import nz.ac.wintec.service.PersistenceService;

@ManagedBean(name = "restrictionController")
@SessionScoped
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RestrictionController extends AbstractController<Restriction> implements Serializable {

    public RestrictionController() {
        super(Restriction.class);
    }

    @PostConstruct
    public void init() {
        try {
            this.setItems(unmarshalConfig().getItems());

            // link the course objects to restrictions
            for (Restriction restriction : this.getItems()) {
                for (Course course : PersistenceService.getManagedBeanInstance(CourseController.class).getItems()) {
                    if (restriction.getCourse().getName().equals(course.getName())) {
                        restriction.setCourse(course);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void saveNew(ActionEvent event) {
        getSelected().setId(Long.valueOf(getItems().size() + 1));
        super.saveNew(event);
    }

    public List<Restriction> getAllItemsOfType(MealType type) {
        List<Restriction> desiredItems = new ArrayList<Restriction>();

        for (Restriction currentRestriction : getItems()) {
            if (currentRestriction.getMeal().toLowerCase().equals(type.getName().toLowerCase())) {
                desiredItems.add(currentRestriction);
            }
        }

        return desiredItems;
    }

    public List<Restriction> getAllItemsOfTypeAndOrder(MealType type, int order) {
        List<Restriction> desiredItems = new ArrayList<Restriction>();

        for (Restriction currentRestriction : getItems()) {
            if (currentRestriction.getMeal().toLowerCase().equals(type.getName().toLowerCase()) && currentRestriction.getOrder() == order) {
                desiredItems.add(currentRestriction);
            }
        }

        return desiredItems;
    }
}
