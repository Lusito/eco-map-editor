package info.lusito.mapeditor.editors.entity;

import info.lusito.mapeditor.editors.properties.api.adapters.PropertyAdapter;

public class EntityProperty extends PropertyAdapter {
    
    private final EcoEntityComponentFX component;

    public EntityProperty(EcoEntityComponentFX component) {
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
