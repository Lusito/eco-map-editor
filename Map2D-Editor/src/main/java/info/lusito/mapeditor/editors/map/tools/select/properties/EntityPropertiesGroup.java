package info.lusito.mapeditor.editors.map.tools.select.properties;

import info.lusito.mapeditor.editors.map.MapEditorController;
import info.lusito.mapeditor.editors.map.model.EcoMapEntityFX;
import info.lusito.mapeditor.editors.map.utils.FocusMode;
import info.lusito.mapeditor.editors.map.utils.UndoableMapEdit;
import info.lusito.mapeditor.editors.properties.api.adapters.PropertiesGroupAdapter;
import info.lusito.mapeditor.editors.properties.api.adapters.PropertyAdapter;
import info.lusito.mapeditor.persistence.map.EcoMapEntity;
import info.lusito.mapeditor.persistence.utils.ParseOrDefault;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoableEdit;

public class EntityPropertiesGroup extends PropertiesGroupAdapter implements CallbackPropertyAdapter.Callback {

    private final MapEditorController controller;
    private EcoMapEntity entity;
    private final PropertyAdapter id = new CallbackPropertyAdapter(this);
    private final PropertyAdapter rotation = new CallbackPropertyAdapter(this);
    private final PropertyAdapter scale = new CallbackPropertyAdapter(this);
    private final PropertyAdapter x = new CallbackPropertyAdapter(this);
    private final PropertyAdapter y = new CallbackPropertyAdapter(this);
    //fixme: tint, file?

    public EntityPropertiesGroup(MapEditorController controller) {
        this.controller = controller;
        name = "Entity";
        description = "Entity properties";
        id.setupString("id", "Choose a unique id which can be used to reference this entity.", null);
        rotation.setupFloat("rotation", "Rotation in degrees", "-360", "360", null);
        scale.setupFloat("scale", "Scale as multiplicator", null, null, null);
        x.setupFloat("x", "X position in pixels (float allowed)", null, null, null);
        y.setupFloat("y", "X position in pixels (float allowed)", null, null, null);
        
        properties.add(id);
        properties.add(rotation);
        properties.add(scale);
        properties.add(x);
        properties.add(y);
    }

    public void setEntity(EcoMapEntity entity) {
        this.entity = entity;
        if(entity == null) {
            name = "Entity";
            description = "Entity properties";
        } else {
            EcoMapEntityFX entityFX = (EcoMapEntityFX) entity.attachment;
            name = "Entity (" + entityFX.definition.name + ")";
            description = entityFX.definition.description;
        }
    }

    @Override
    public void setValue(PropertyAdapter adapter, String value) {
        if(entity != null) {
            String oldValue = getValue(adapter);
            doSetValue(adapter, value);
            controller.addUndoableEdit(new UndoablePropertyChange(controller, adapter, oldValue, value));
        }
    }

    private void doSetValue(PropertyAdapter adapter, String value) {
        if(adapter == id)
            entity.id = value.isEmpty() ? null : value;
        else if(adapter == rotation)
            entity.rotation = ParseOrDefault.getFloat(value, 0);
        else if(adapter == scale)
            entity.scale = ParseOrDefault.getFloat(value, 1);
        else if(adapter == x)
            entity.x = ParseOrDefault.getFloat(value, 0);
        else if(adapter == y)
            entity.y = ParseOrDefault.getFloat(value, 0);
        EcoMapEntityFX entityFX = (EcoMapEntityFX) entity.attachment;
        entityFX.updateRect(entity);
    }

    @Override
    public String getValue(PropertyAdapter adapter) {
        if(entity != null) {
            if(adapter == id)
                return entity.id == null ? "" : entity.id;
            else if(adapter == rotation)
                return "" + entity.rotation;
            else if(adapter == scale)
                return "" + entity.scale;
            else if(adapter == x)
                return "" + entity.x;
            else if(adapter == y)
                return "" + entity.y;
        }
        return null;
    }
    
    private class UndoablePropertyChange extends UndoableMapEdit {

        private final PropertyAdapter adapter;
        private final String from;
        private String to;

        public UndoablePropertyChange(MapEditorController controller, PropertyAdapter adapter, String from, String to) {
            super(controller);
            this.adapter = adapter;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            doSetValue(adapter, from);
            applyLayerFocus(FocusMode.SELECTION);
            controller.getPropertiesEditor().updateValues();
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            doSetValue(adapter, to);
            applyLayerFocus(FocusMode.SELECTION);
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
                if(edit.getEntity() == entity) {
                    to = edit.to;
                    consolidatedCount++;
                    return true;
                }
            }
            return false;
        }

        private EcoMapEntity getEntity() {
            return entity;
        }
    }
    
}
