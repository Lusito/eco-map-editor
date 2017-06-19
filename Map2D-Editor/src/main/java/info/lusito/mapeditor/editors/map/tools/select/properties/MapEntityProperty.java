package info.lusito.mapeditor.editors.map.tools.select.properties;

import info.lusito.mapeditor.editors.properties.api.adapters.PropertyAdapter;

public class MapEntityProperty extends PropertyAdapter {

    private final MapEntityComponent component;

    public MapEntityProperty(MapEntityComponent component) {
        this.component = component;
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        component.setPropertyValue(name, value);
    }

    @Override
    public String getValue() {
        return component.getPropertyValue(name);
    }
}
