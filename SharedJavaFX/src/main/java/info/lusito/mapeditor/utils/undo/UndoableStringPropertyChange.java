package info.lusito.mapeditor.utils.undo;

import javafx.beans.property.StringProperty;

public class UndoableStringPropertyChange extends AbstractUndoablePropertyChange<StringProperty, String> {

    public UndoableStringPropertyChange(UndoContext listener, StringProperty property, String oldValue, String newValue) {
        super(listener, property, oldValue, newValue);
    }

    @Override
    protected void setValue(String value) {
        property.setValue(value);
    }
}
