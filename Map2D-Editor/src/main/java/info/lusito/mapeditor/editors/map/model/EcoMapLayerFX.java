package info.lusito.mapeditor.editors.map.model;

import info.lusito.mapeditor.editors.map.MapEditorController;
import info.lusito.mapeditor.editors.map.utils.FocusMode;
import info.lusito.mapeditor.editors.map.utils.UndoableMapEdit;
import info.lusito.mapeditor.editors.properties.api.adapters.PropertiesAdapter;
import info.lusito.mapeditor.persistence.map.EcoMapLayer;
import info.lusito.mapeditor.persistence.map.EcoMapLayerType;
import java.util.ArrayList;
import java.util.List;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoableEdit;

public class EcoMapLayerFX implements LayerInterface {

    private final EcoMapLayer layer;
    private final EcoMapFX map;
    private final MapEditorController controller;
    public List selection = new ArrayList();

    public EcoMapLayerFX(EcoMapLayer layer, EcoMapFX map) {
        this.layer = layer;
        this.map = map;
        controller = map.getController();
    }

    @Override
    public EcoMapLayerType getType() {
        return layer.getType();
    }

    @Override
    public String getName() {
        return layer.name;
    }

    @Override
    public void setName(String value) {
        if(!value.equals(layer.name)) {
            String from = layer.name;
            layer.name = value;
            controller.addUndoableEdit(new UndoableLayerNameChange(controller, from, value));
        }
    }

    @Override
    public boolean isLocked() {
        return layer.locked;
    }

    @Override
    public void setLocked(boolean value) {
        if(layer.locked != value) {
            boolean from = layer.locked;
            layer.locked = value;
            controller.addUndoableEdit(new UndoableLayerLockedChange(controller, from, value));
        }
    }

    @Override
    public boolean isVisible() {
        return layer.visible;
    }

    @Override
    public void setVisible(boolean value) {
        if(layer.visible != value) {
            boolean from = layer.visible;
            layer.visible = value;
            controller.addUndoableEdit(new UndoableLayerVisibleChange(controller, from, value));
            map.requestRendering();
        }
    }

    @Override
    public float getOpacity() {
        return layer.opacity;
    }

    @Override
    public void setOpacity(float value) {
        if(layer.opacity != value) {
            float from = layer.opacity;
            layer.opacity = value;
            controller.addUndoableEdit(new UndoableLayerOpacityChange(controller, from, value));
            map.requestRendering();
        }
    }

    @Override
    public String toString() {
        return layer.name;
    }

    @Override
    public void focus() {
        map.focusLayer(this, FocusMode.LAYER);
        map.requestRendering(); //todo: probably can be removed in final stage
    }

    @Override
    public void showProperties() {
        PropertiesAdapter editor = controller.getPropertiesEditor();
        editor.groups.clear();
        editor.loaded = true;
        editor.title = "Layer";
        editor.instance = false;
        editor.updateEverything();
        //Fixme
    }

    @Override
    public void highlight() {
        // fixme: create undo entry?
        map.highlightLayer(this);
    }

    @Override
    public void duplicate() {
        map.duplicateLayer(this);
    }

    @Override
    public void remove() {
        map.removeLayer(this);
    }

    public EcoMapLayer getLayer() {
        return layer;
    }
    
    private void updateMapLayers() {
        map.focusLayer(this, FocusMode.LAYER);
        map.update(MapUpdateType.LAYERS);
    }

    private class UndoableLayerNameChange extends UndoableMapEdit {

        private final String from;
        private String to;

        public UndoableLayerNameChange(MapEditorController controller, String from, String to) {
            super(controller);
            this.from = from;
            this.to = to;
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            layer.name = from;
            updateMapLayers();
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            layer.name = to;
            updateMapLayers();
        }

        @Override
        public boolean isSignificant() {
            return !from.equals(to);
        }
        
        @Override
        public boolean addEdit(UndoableEdit anEdit) {
            if (anEdit instanceof UndoableLayerNameChange) {
                UndoableLayerNameChange edit = (UndoableLayerNameChange) anEdit;
                if(edit.getLayer() == layer) {
                    to = edit.to;
                    consolidatedCount++;
                    return true;
                }
            }
            return false;
        }

        private EcoMapLayer getLayer() {
            return layer;
        }
    }

    private class UndoableLayerLockedChange extends UndoableMapEdit {

        private final boolean from;
        private boolean to;

        public UndoableLayerLockedChange(MapEditorController controller, boolean from, boolean to) {
            super(controller);
            this.from = from;
            this.to = to;
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            layer.locked = from;
            updateMapLayers();
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            layer.locked = to;
            updateMapLayers();
        }

        @Override
        public boolean isSignificant() {
            return from != to;
        }
        
        @Override
        public boolean addEdit(UndoableEdit anEdit) {
            if (anEdit instanceof UndoableLayerLockedChange) {
                UndoableLayerLockedChange edit = (UndoableLayerLockedChange) anEdit;
                if(edit.getLayer() == layer) {
                    to = edit.to;
                    consolidatedCount++;
                    return true;
                }
            }
            return false;
        }

        private EcoMapLayer getLayer() {
            return layer;
        }
    }

    private class UndoableLayerVisibleChange extends UndoableMapEdit {

        private final boolean from;
        private boolean to;

        public UndoableLayerVisibleChange(MapEditorController controller, boolean from, boolean to) {
            super(controller);
            this.from = from;
            this.to = to;
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            layer.visible = from;
            updateMapLayers();
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            layer.visible = to;
            updateMapLayers();
        }

        @Override
        public boolean isSignificant() {
            return from != to;
        }
        
        @Override
        public boolean addEdit(UndoableEdit anEdit) {
            if (anEdit instanceof UndoableLayerVisibleChange) {
                UndoableLayerVisibleChange edit = (UndoableLayerVisibleChange) anEdit;
                if(edit.getLayer() == layer) {
                    to = edit.to;
                    consolidatedCount++;
                    return true;
                }
            }
            return false;
        }

        private EcoMapLayer getLayer() {
            return layer;
        }
    }

    private class UndoableLayerOpacityChange extends UndoableMapEdit {

        private final float from;
        private float to;

        public UndoableLayerOpacityChange(MapEditorController controller, float from, float to) {
            super(controller);
            this.from = from;
            this.to = to;
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            layer.opacity = from;
            updateMapLayers();
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            layer.opacity = to;
            updateMapLayers();
        }

        @Override
        public boolean isSignificant() {
            return from != to;
        }
        
        @Override
        public boolean addEdit(UndoableEdit anEdit) {
            if (anEdit instanceof UndoableLayerOpacityChange) {
                UndoableLayerOpacityChange edit = (UndoableLayerOpacityChange) anEdit;
                if(edit.getLayer() == layer) {
                    to = edit.to;
                    consolidatedCount++;
                    return true;
                }
            }
            return false;
        }

        private EcoMapLayer getLayer() {
            return layer;
        }
    }
}
