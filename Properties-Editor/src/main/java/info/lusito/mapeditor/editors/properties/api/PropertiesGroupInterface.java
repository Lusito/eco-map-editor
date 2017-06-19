package info.lusito.mapeditor.editors.properties.api;

import java.util.List;

public interface PropertiesGroupInterface {

    String getName();

    String getDescription();

    List<PropertyInterface> getProperties();
}
