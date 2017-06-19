package info.lusito.mapeditor.editors.properties.api;

import java.util.List;

public interface PropertyInterface {

    String getName();

    String getDescription();

    PropertyType getType();

    boolean getMultiple();

    String getMinimum();

    String getMaximum();

    List<String> getPossibleValues();

    String getValue();

    String getDefaultValue();

    void setValue(String value);
}
