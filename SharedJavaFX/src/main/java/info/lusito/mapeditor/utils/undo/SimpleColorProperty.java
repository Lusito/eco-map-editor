package info.lusito.mapeditor.utils.undo;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

public class SimpleColorProperty extends SimpleObjectProperty<Color> {

    public SimpleColorProperty(Color initialValue) {
        super(initialValue);
    }
    
}
