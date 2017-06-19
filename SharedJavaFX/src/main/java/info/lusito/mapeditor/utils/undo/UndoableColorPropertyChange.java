package info.lusito.mapeditor.utils.undo;

import javafx.scene.paint.Color;

public class UndoableColorPropertyChange extends AbstractUndoablePropertyChange<SimpleColorProperty, Color> {

    public UndoableColorPropertyChange(UndoContext listener, SimpleColorProperty property, Color oldValue, Color newValue) {
        super(listener, property, oldValue, newValue);
    }

    @Override
    protected void setValue(Color value) {
        property.setValue(value);
    }
}
