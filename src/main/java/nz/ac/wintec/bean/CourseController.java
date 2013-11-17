package nz.ac.wintec.bean;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import nz.ac.wintec.entity.Course;
import nz.ac.wintec.entity.Ingredient;
import nz.ac.wintec.entity.Meal;
import nz.ac.wintec.entity.Restriction;
import nz.ac.wintec.service.PersistenceService;

@ManagedBean(name = "courseController")
@SessionScoped
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CourseController extends AbstractController<Course> implements Serializable {

    public CourseController() {
        super(Course.class);
    }

    @PostConstruct
    public void init() {
        try {
            this.setItems(unmarshalConfig().getItems());
            
            // link the ingredient objects to courses
            for (Course course : this.getItems()) {
                for (int index = 0; index < course.getIngredients().size(); index++) {
                    for (Ingredient ingredient : PersistenceService.getManagedBeanInstance(IngredientController.class).getItems()) {
                        if (course.getIngredients().get(index).getName().equals(ingredient.getName())) {
                            course.getIngredients().set(index, ingredient);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void saveNew(ActionEvent event) {
        getSelected().setId(Long.valueOf(getItems().size() + 1));
        super.saveNew(event);
    }
}
