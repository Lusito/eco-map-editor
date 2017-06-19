package info.lusito.mapeditor.editors.map.tools.select.properties;

import info.lusito.mapeditor.editors.map.MapEditorController;
import info.lusito.mapeditor.editors.map.utils.FocusMode;
import info.lusito.mapeditor.editors.map.utils.UndoableMapEdit;
import info.lusito.mapeditor.editors.properties.api.PropertiesGroupInterface;
import info.lusito.mapeditor.editors.properties.api.PropertyInterface;
import info.lusito.mapeditor.editors.properties.api.PropertyType;
import info.lusito.mapeditor.persistence.component.EcoComponent;
import info.lusito.mapeditor.persistence.component.EcoComponentProperty;
import info.lusito.mapeditor.persistence.entity.EcoEntityComponent;
import info.lusito.mapeditor.persistence.map.EcoMapEntityComponent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoableEdit;

public class MapEntityComponent implements PropertiesGroupInterface {

    private final MapEditorController controller;
    private final List<PropertyInterface> properties = new CopyOnWriteArrayList();
    private final EcoComponent component;
    private final EcoEntityComponent entityComponent;
    private final EcoMapEntityComponent mapEntityComponent;

    public MapEntityComponent(MapEditorController controller,
            EcoComponent component, EcoEntityComponent entityComponent,
            EcoMapEntityComponent mapEntityComponent) {
        this.controller = controller;
        this.component = component;
        this.entityComponent = entityComponent;
        this.mapEntityComponent = mapEntityComponent;
        
        for (EcoComponentProperty property : component.properties) {
            properties.add(getAdapter(property));
        }
    }

    private MapEntityProperty getAdapter(EcoComponentProperty property) {
        MapEntityProperty adapter = new MapEntityProperty(this);

        adapter.name = property.name;
        adapter.description = property.description;
        adapter.type = PropertyType.getSafe(property.type);
        adapter.multiple = property.multiple == null ? false : property.multiple;
        adapter.minimum = property.minimum;
        adapter.maximum = property.maximum;
        if (property.values == null) {
            adapter.possibleValues = null;
        } else {
            adapter.possibleValues = new CopyOnWriteArrayList();
            for (String value : property.values.split("\n")) {
                adapter.possibleValues.add(value);
            }
        }

        adapter.value = mapEntityComponent.properties.get(adapter.name);
        adapter.defaultValue = entityComponent.properties.get(adapter.name);
        return adapter;
    }

    @Override
    public String getName() {
        return component.name;
    }

    @Override
    public String getDescription() {
        return component.description;
    }

    @Override
    public List<PropertyInterface> getProperties() {
        return properties;
    }

    void setPropertyValue(String key, String value) {
        String oldValue = getPropertyValue(key);
        doSetPropertyValue(key, value);
        controller.addUndoableEdit(new UndoablePropertyChange(controller, key, oldValue, value));
    }

    private void doSetPropertyValue(String key, String value) {
        if (value == null) {
            mapEntityComponent.properties.remove(key);
        } else {
            mapEntityComponent.properties.put(key, value);
        }
    }

    String getPropertyValue(String key) {
        return mapEntityComponent.properties.get(key);
    }
    
    private class UndoablePropertyChange extends UndoableMapEdit {

        private final String key;
        private final String from;
        private String to;

        public UndoablePropertyChange(MapEditorController controller, String key, String from, String to) {
            super(controller);
            this.key = key;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            applyLayerFocus(FocusMode.SELECTION);
            doSetPropertyValue(key, from);
            controller.getPropertiesEditor().updateValues();
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            applyLayerFocus(FocusMode.SELECTION);
            doSetPropertyValue(key, to);
            controller.getPropertiesEditor().updateValues();
        }

        @Override
        public boolean isSignificant() {
            if(from == null)
                return to != null;
            return !from.equals(to);
        }
        
        @Override
        public boolean addEdit(UndoableEdit anEdit) {
            if (anEdit instanceof UndoablePropertyChange) {
                UndoablePropertyChange edit = (UndoablePropertyChange) anEdit;
                if(edit.getMapEntityComponent() == mapEntityComponent) {
                    to = edit.to;
                    consolidatedCount++;
                    return true;
                }
            }
            return false;
        }

        private EcoMapEntityComponent getMapEntityComponent() {
            return mapEntityComponent;
        }
    }
}
