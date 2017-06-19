package info.lusito.mapeditor.common;

import info.lusito.mapeditor.utils.PropertyFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;

public class EcoPropertyFX {

    private final SimpleStringProperty key;
    private final SimpleStringProperty value;

    public EcoPropertyFX(String key, String value) {
        this.key = PropertyFactory.createString(key);
        this.value = PropertyFactory.createString(value);
    }

    public void addListeners(ChangeListener<String> stringListener) {
        key.addListener(stringListener);
        value.addListener(stringListener);
    }

    public void removeListeners(ChangeListener<String> stringListener) {
        key.removeListener(stringListener);
        value.removeListener(stringListener);
    }

    public SimpleStringProperty keyProperty() {
        return key;
    }

    public String getKey() {
        return key.get();
    }

    public void setKey(String value) {
        key.set(value);
    }

    public SimpleStringProperty valueProperty() {
        return value;
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
    }
}
