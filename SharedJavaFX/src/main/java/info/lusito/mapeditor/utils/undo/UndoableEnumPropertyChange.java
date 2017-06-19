package info.lusito.mapeditor.utils.undo;

public class UndoableEnumPropertyChange extends AbstractUndoablePropertyChange<SimpleEnumProperty, Enum> {

    public UndoableEnumPropertyChange(UndoContext listener, SimpleEnumProperty property, Enum oldValue, Enum newValue) {
        super(listener, property, oldValue, newValue);
    }

    @Override
    protected void setValue(Enum value) {
        property.setValue(value);
    }
}
