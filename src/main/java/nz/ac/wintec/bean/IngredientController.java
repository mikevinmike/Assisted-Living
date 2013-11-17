package nz.ac.wintec.bean;

import nz.ac.wintec.entity.Ingredient;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@ManagedBean(name = "ingredientController")
@SessionScoped
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class IngredientController extends AbstractController<Ingredient> implements Serializable {

    public IngredientController() {
        super(Ingredient.class);
    }
    
    @PostConstruct
    public void init() {
        try {
            this.setItems(unmarshalConfig().getItems());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void saveNew(ActionEvent event) {
        getSelected().setId(Long.valueOf(getItems().size() + 1));
        super.saveNew(event);
    }
}
