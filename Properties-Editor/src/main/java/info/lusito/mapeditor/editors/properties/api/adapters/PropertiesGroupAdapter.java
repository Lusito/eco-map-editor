package info.lusito.mapeditor.editors.properties.api.adapters;

import info.lusito.mapeditor.editors.properties.api.PropertiesGroupInterface;
import info.lusito.mapeditor.editors.properties.api.PropertyInterface;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PropertiesGroupAdapter implements PropertiesGroupInterface {

    public String name;
    public String description;
    public final List<PropertyInterface> properties = new CopyOnWriteArrayList();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<PropertyInterface> getProperties() {
        return properties;
    }

}
