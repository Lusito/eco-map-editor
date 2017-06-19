package info.lusito.mapeditor.editors.map.tools.select.properties;

import info.lusito.mapeditor.editors.map.MapEditorController;
import info.lusito.mapeditor.editors.map.model.EcoMapImageFX;
import info.lusito.mapeditor.editors.map.utils.FocusMode;
import info.lusito.mapeditor.editors.map.utils.UndoableMapEdit;
import info.lusito.mapeditor.model.EcoMapImageGDX;
import info.lusito.mapeditor.editors.properties.api.adapters.PropertiesGroupAdapter;
import info.lusito.mapeditor.editors.properties.api.adapters.PropertyAdapter;
import info.lusito.mapeditor.persistence.map.EcoMapImage;
import info.lusito.mapeditor.persistence.utils.ParseOrDefault;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoableEdit;

public class ImagePropertiesGroup extends PropertiesGroupAdapter implements CallbackPropertyAdapter.Callback {

    private final MapEditorController controller;
    private EcoMapImage image;
    private final PropertyAdapter rotation = new CallbackPropertyAdapter(this);
    private final PropertyAdapter scale = new CallbackPropertyAdapter(this);
    private final PropertyAdapter x = new CallbackPropertyAdapter(this);
    private final PropertyAdapter y = new CallbackPropertyAdapter(this);
    //fixme: tint, file?

    public ImagePropertiesGroup(MapEditorController controller) {
        this.controller = controller;
        name = "Image";
        description = "Image properties";
        rotation.setupFloat("rotation", "Rotation in degrees", "-360", "360", null);
        scale.setupFloat("scale", "Scale as multiplicator", null, null, null);
        x.setupFloat("x", "X position in pixels (float allowed)", null, null, null);
        y.setupFloat("y", "X position in pixels (float allowed)", null, null, null);
        
        properties.add(rotation);
        properties.add(scale);
        properties.add(x);
        properties.add(y);
    }

    public void setImage(EcoMapImage image) {
        this.image = image;
        if(image == null) {
            name = "Image";
            description = "Image properties";
        } else {
            EcoMapImageGDX imageFX = (EcoMapImageGDX) image.attachment;
            description = imageFX.getRelativePath();
            String[] parts = description.split("/");
            if(parts.length == 0) {
                name = "Image (" + description + ")";
            } else {
                name = "Image (" + parts[parts.length-1] + ")";
            }
        }
    }

    @Override
    public void setValue(PropertyAdapter adapter, String value) {
        if(image != null) {
            String oldValue = getValue(adapter);
            doSetValue(adapter, value);
            controller.addUndoableEdit(new UndoablePropertyChange(controller, adapter, oldValue, value));
        }
    }

    private void doSetValue(PropertyAdapter adapter, String value) {
        if(adapter == rotation)
            image.rotation = ParseOrDefault.getFloat(value, 0);
        else if(adapter == scale)
            image.scale = ParseOrDefault.getFloat(value, 1);
        else if(adapter == x)
            image.x = ParseOrDefault.getFloat(value, 0);
        else if(adapter == y)
            image.y = ParseOrDefault.getFloat(value, 0);
        EcoMapImageFX imageFX = (EcoMapImageFX) image.attachment;
        imageFX.updateRect(image);
    }

    @Override
    public String getValue(PropertyAdapter adapter) {
        if(image != null) {
            if(adapter == rotation)
                return "" + image.rotation;
            else if(adapter == scale)
                return "" + image.scale;
            else if(adapter == x)
                return "" + image.x;
            else if(adapter == y)
                return "" + image.y;
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
                if(edit.getImage() == image) {
                    to = edit.to;
                    consolidatedCount++;
                    return true;
                }
            }
            return false;
        }

        private EcoMapImage getImage() {
            return image;
        }
    }
    
}
