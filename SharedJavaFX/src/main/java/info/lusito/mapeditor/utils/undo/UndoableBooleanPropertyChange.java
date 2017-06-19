package info.lusito.mapeditor.utils.undo;

import javafx.beans.property.SimpleBooleanProperty;

public class UndoableBooleanPropertyChange extends AbstractUndoablePropertyChange<SimpleBooleanProperty, Boolean> {

    public UndoableBooleanPropertyChange(UndoContext listener, SimpleBooleanProperty property, Boolean oldValue, Boolean newValue) {
        super(listener, property, oldValue, newValue);
    }

    @Override
    protected void setValue(Boolean value) {
        property.setValue(value);
    }
}
