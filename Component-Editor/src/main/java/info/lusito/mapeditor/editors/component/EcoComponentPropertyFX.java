package info.lusito.mapeditor.editors.component;

import info.lusito.mapeditor.editors.properties.api.PropertyInterface;
import info.lusito.mapeditor.editors.properties.api.PropertyType;
import info.lusito.mapeditor.utils.undo.SimpleEnumProperty;
import info.lusito.mapeditor.utils.PropertyFactory;
import info.lusito.mapeditor.persistence.component.EcoComponentProperty;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;

public class EcoComponentPropertyFX implements PropertyInterface {

    private final SimpleStringProperty name;
    private final SimpleStringProperty description;
    private final SimpleEnumProperty<PropertyType> type;
    private final SimpleBooleanProperty multiple;
    private final SimpleStringProperty minimum;
    private final SimpleStringProperty maximum;
    private final SimpleStringProperty values;

    EcoComponentPropertyFX(String name, String type) {
        this.name = PropertyFactory.createString(name);
        this.description = PropertyFactory.createString("");
        this.type = PropertyFactory.createEnum(PropertyType.getSafe(type));
        this.multiple = PropertyFactory.createBoolean(true);
        this.minimum = PropertyFactory.createString("");
        this.maximum = PropertyFactory.createString("");
        this.values = PropertyFactory.createString("");
    }

    EcoComponentPropertyFX(EcoComponentProperty cpd) {
        this.name = PropertyFactory.createString(cpd.name);
        this.description = PropertyFactory.createString(cpd.description);
        this.type = PropertyFactory.createEnum(PropertyType.getSafe(cpd.type));
        this.multiple = PropertyFactory.createBoolean(cpd.multiple);
        this.minimum = PropertyFactory.createString(cpd.minimum);
        this.maximum = PropertyFactory.createString(cpd.maximum);
        this.values = PropertyFactory.createString(cpd.values);
    }
    
    void addListeners(ChangeListener<String> stringListener, ChangeListener<Boolean> booleanListener, ChangeListener<Enum> enumListener) {
        name.addListener(stringListener);
        description.addListener(stringListener);
        type.addListener(enumListener);
        multiple.addListener(booleanListener);
        minimum.addListener(stringListener);
        maximum.addListener(stringListener);
        values.addListener(stringListener);
    }

    void removeListeners(ChangeListener<String> stringListener, ChangeListener<Boolean> booleanListener, ChangeListener<Enum> enumListener) {
        name.removeListener(stringListener);
        description.removeListener(stringListener);
        type.removeListener(enumListener);
        multiple.removeListener(booleanListener);
        minimum.removeListener(stringListener);
        maximum.removeListener(stringListener);
        values.removeListener(stringListener);
    }

    
    public SimpleStringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String value) {
        name.set(value);
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String value) {
        description.set(value);
    }

    public SimpleEnumProperty typeProperty() {
        return type;
    }

    public void setType(String value) {
        type.set(PropertyType.getSafe(value));
    }

    public SimpleBooleanProperty multipleProperty() {
        return multiple;
    }

    public boolean getMultiple() {
        return multiple.get();
    }

    public void setMultiple(boolean value) {
        multiple.set(value);
    }

    public SimpleStringProperty minimumProperty() {
        return minimum;
    }

    public String getMinimum() {
        return minimum.get();
    }

    public void setMinimum(String value) {
        minimum.set(value);
    }

    public SimpleStringProperty maximumProperty() {
        return maximum;
    }

    public String getmaximum() {
        return maximum.get();
    }

    public void setMaximum(String value) {
        maximum.set(value);
    }

    public SimpleStringProperty valuesProperty() {
        return values;
    }

    public String getValues() {
        return values.get();
    }

    public void setValues(String value) {
        values.set(value);
    }

    @Override
    public PropertyType getType() {
        return type.get();
    }

    @Override
    public String getMaximum() {
        return maximum.get();
    }

    @Override
    public List<String> getPossibleValues() {
        return Arrays.asList(values.get().split("\r?\n"));
    }

    @Override
    public String getValue() {
        return getDefaultValue();
    }

    @Override
    public String getDefaultValue() {
        switch(getType()) {
            case STRING:
                return "Foo Bar";
            case MULTILINE_STRING:
                return "a=hello world\nb=foo bar";
            case FLOAT:
                return "3.14";
            case INTEGER:
                return "42";
            case FILE:
                return "path/file.ext";
            case COLOR:
                return "ff0000";
            case SLIDER:
                return "50";
            case BOOLEAN:
                return "true";
            case ENUM:
                List<String> list = getPossibleValues();
                return list.isEmpty() ? "" : list.get(0);
            default:
                return "";
        }
    }

    @Override
    public void setValue(String value) {
    }
}
