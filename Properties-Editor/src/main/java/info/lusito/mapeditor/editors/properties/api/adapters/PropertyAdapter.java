package info.lusito.mapeditor.editors.properties.api.adapters;

import info.lusito.mapeditor.editors.properties.api.PropertyInterface;
import info.lusito.mapeditor.editors.properties.api.PropertyType;
import java.util.List;

public class PropertyAdapter implements PropertyInterface {

    public String name;
    public String description;
    public PropertyType type;
    public boolean multiple;
    public String minimum;
    public String maximum;
    public List<String> possibleValues;
    public String value;
    public String defaultValue;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public PropertyType getType() {
        return type;
    }

    @Override
    public boolean getMultiple() {
        return multiple;
    }

    @Override
    public String getMinimum() {
        return minimum;
    }

    @Override
    public String getMaximum() {
        return maximum;
    }

    @Override
    public List<String> getPossibleValues() {
        return possibleValues;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    
    public void setupFloat(String name, String description, String minimum, String maximum, String defaultValue) {
        type = PropertyType.FLOAT;
        this.name = name;
        this.description = description;
        this.minimum = minimum;
        this.maximum = maximum;
        this.defaultValue = defaultValue;
    }

    public void setupString(String name, String description, String defaultValue) {
        type = PropertyType.STRING;
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    public void setupMultilineString(String name, String description, String defaultValue) {
        type = PropertyType.MULTILINE_STRING;
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
    }
}
