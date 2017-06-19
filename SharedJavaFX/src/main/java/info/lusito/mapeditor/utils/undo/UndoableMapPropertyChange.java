package info.lusito.mapeditor.utils.undo;

import javafx.beans.property.SimpleMapProperty;
import javax.swing.undo.UndoableEdit;

public class UndoableMapPropertyChange extends AbstractUndoablePropertyChange<SimpleMapProperty<String,String>, String> {
    
    protected String key;

    public UndoableMapPropertyChange(UndoContext listener, SimpleMapProperty<String,String> property, String key, String oldValue, String newValue) {
        super(listener, property, oldValue, newValue);
        this.key = key;
    }

    @Override
    protected void setValue(String value) {
        if(value == null)
            property.remove(key);
        else
            property.put(key, value);
    }

    @Override
    public boolean isSignificant() {
        if(oldValue == null)
            return newValue != null;
        return !oldValue.equals(newValue);
    }

    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        if (property == context.lastUndoProperty && anEdit instanceof UndoableMapPropertyChange) {
            UndoableMapPropertyChange anEdit2 = (UndoableMapPropertyChange) anEdit;
            if(key.equals(anEdit2.key)) {
                newValue = anEdit2.newValue;
                consolidatedCount++;
                return true;
            }
        }
        return false;
    }
}
