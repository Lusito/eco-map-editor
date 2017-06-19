package info.lusito.mapeditor.editors.map.layerview;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;

public class ToggleImageView extends ImageView {

    public ToggleImageView(Image defaultIcon, Image selectedIcon) {
        setImage(defaultIcon);
        setPickOnBounds(true);
        selected.addListener((obs, oldValue, newValue) -> {
            setImage(newValue ? selectedIcon : defaultIcon);
        });
        setOnMouseClicked((e)-> {
            if(e.getButton() == MouseButton.PRIMARY) {
                selected.set(!selected.get());
            }
        });
    }
    
    /**
     * Indicates whether this CheckBox is checked.
     */
    private final BooleanProperty selected = new SimpleBooleanProperty();
    public final void setSelected(boolean value) {
        selected.set(value);
    }

    public final boolean isSelected() {
        return selected.get();
    }

    public final BooleanProperty selectedProperty() {
        return selected;
    }
}
