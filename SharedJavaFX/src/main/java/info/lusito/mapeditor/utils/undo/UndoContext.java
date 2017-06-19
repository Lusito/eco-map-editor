package info.lusito.mapeditor.utils.undo;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.scene.paint.Color;
import javax.swing.undo.UndoableEdit;

public abstract class UndoContext {

    private boolean unmodifiedReachable = true;
    private int unmodifiedOffset;
    protected boolean performingUndoRedo;
    protected Object lastUndoProperty;
    protected final StringPropertyListener stringPropertyListener = new StringPropertyListener();
    protected final BooleanPropertyListener booleanPropertyListener = new BooleanPropertyListener();
    protected final EnumPropertyListener enumPropertyListener = new EnumPropertyListener();
    protected final MapPropertyListener mapPropertyListener = new MapPropertyListener();
    protected final ColorPropertyListener colorPropertyListener = new ColorPropertyListener();

    public abstract void setModified(boolean modified);

    public abstract void addUndoableEdit(UndoableEdit edit);

    public boolean isPerformingUndoRedo() {
        return performingUndoRedo;
    }
    
    protected void onUndoableEditAdded() {
        if(unmodifiedOffset < 0) {
            unmodifiedReachable = false;
            unmodifiedOffset = 1;
        } else {
            unmodifiedOffset++;
        }
        setModified(true);
    }
    
    protected void markUnmodified() {
        unmodifiedOffset = 0;
        unmodifiedReachable = true;
        setModified(false);
    }

    protected void beforeUndo(int consolidatedCount) {
        unmodifiedOffset -= consolidatedCount;
        setModified(!unmodifiedReachable || unmodifiedOffset != 0);
        performingUndoRedo = true;
    }

    protected void afterUndo() {
        performingUndoRedo = false;
    }

    protected void beforeRedo(int consolidatedCount) {
        unmodifiedOffset += consolidatedCount;
        setModified(!unmodifiedReachable || unmodifiedOffset != 0);
        performingUndoRedo = true;
    }

    protected void afterRedo() {
        performingUndoRedo = false;
    }

    public void setLastUndoProperty(Object value) {
        lastUndoProperty = value;
    }

    public void addUndo(StringProperty property, String oldValue, String newValue) {
        if (!performingUndoRedo) {
            lastUndoProperty = property;
            addUndoableEdit(new UndoableStringPropertyChange(this, property, oldValue, newValue));
        }
    }

    public void addUndo(SimpleBooleanProperty property, Boolean oldValue, Boolean newValue) {
        if (!isPerformingUndoRedo()) {
            lastUndoProperty = property;
            addUndoableEdit(new UndoableBooleanPropertyChange(this, property, oldValue, newValue));
        }
    }

    public void addUndo(SimpleEnumProperty property, Enum oldValue, Enum newValue) {
        if (!isPerformingUndoRedo()) {
            lastUndoProperty = property;
            addUndoableEdit(new UndoableEnumPropertyChange(this, property, oldValue, newValue));
        }
    }

    public void addUndo(SimpleMapProperty<String, String> property, String key, String oldValue, String newValue) {
        if (!isPerformingUndoRedo()) {
            lastUndoProperty = property;
            addUndoableEdit(new UndoableMapPropertyChange(this, property, key, oldValue, newValue));
        }
    }

    public void addUndo(SimpleColorProperty property, Color oldValue, Color newValue) {
        if (!isPerformingUndoRedo()) {
            lastUndoProperty = property;
            addUndoableEdit(new UndoableColorPropertyChange(this, property, oldValue, newValue));
        }
    }

    public void addPropertyListeners(Object o) {
    }

    public void removePropertyListeners(Object o) {
    }

    public class StringPropertyListener implements ChangeListener<String> {

        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            addUndo((StringProperty) observable, oldValue, newValue);
        }
    }

    public class BooleanPropertyListener implements ChangeListener<Boolean> {

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            addUndo((SimpleBooleanProperty) observable, oldValue, newValue);
        }
    }

    public class EnumPropertyListener implements ChangeListener<Enum> {

        @Override
        public void changed(ObservableValue<? extends Enum> observable, Enum oldValue, Enum newValue) {
            addUndo((SimpleEnumProperty) observable, oldValue, newValue);
        }
    }

    public class MapPropertyListener implements MapChangeListener<String, String> {

        @Override
        public void onChanged(Change<? extends String, ? extends String> change) {
            addUndo((SimpleMapProperty) change.getMap(), change.getKey(), change.getValueRemoved(), change.getValueAdded());
        }
    }


    public class ColorPropertyListener implements ChangeListener<Color> {

        @Override
        public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
            addUndo((SimpleColorProperty) observable, oldValue, newValue);
        }
    }
}
