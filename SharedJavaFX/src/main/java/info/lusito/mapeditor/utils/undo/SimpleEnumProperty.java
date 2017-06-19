package info.lusito.mapeditor.utils.undo;

import javafx.beans.property.SimpleObjectProperty;

public class SimpleEnumProperty<T> extends SimpleObjectProperty<T> {

    public SimpleEnumProperty(T initialValue) {
        super(initialValue);
    }
    
}
