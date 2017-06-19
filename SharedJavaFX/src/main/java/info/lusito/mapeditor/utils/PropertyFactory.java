package info.lusito.mapeditor.utils;

import info.lusito.mapeditor.utils.undo.SimpleEnumProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;

public class PropertyFactory {
    
    public static SimpleStringProperty createString(String value) {
        return new SimpleStringProperty(value == null ? "" : value);
    }
    
    public static SimpleBooleanProperty createBoolean(Boolean value) {
        return new SimpleBooleanProperty(value == null ? Boolean.FALSE : value);
    }
    
    public static SimpleFloatProperty createFloat(Float value) {
        return new SimpleFloatProperty(value == null ? 0 : value);
    }

    public static SimpleEnumProperty createEnum(Enum value) {
        return new SimpleEnumProperty(value == null ? 0 : value);
    }
}
