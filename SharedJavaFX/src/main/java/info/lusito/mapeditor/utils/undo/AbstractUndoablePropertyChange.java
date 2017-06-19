package info.lusito.mapeditor.utils.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

public abstract class AbstractUndoablePropertyChange<PT, VT> extends AbstractUndoableEditFX {

    protected final PT property;
    protected final VT oldValue;
    protected VT newValue;

    public AbstractUndoablePropertyChange(UndoContext listener, PT property, VT oldValue, VT newValue) {
        super(listener);
        listener.lastUndoProperty = property;
        this.property = property;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    protected abstract void setValue(VT value);

    @Override
    protected void performUndo() throws CannotUndoException {
        setValue(oldValue);
    }

    @Override
    protected void performRedo() throws CannotRedoException {
        setValue(newValue);
    }

    @Override
    public boolean isSignificant() {
        return !oldValue.equals(newValue);
    }

    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        if (property == context.lastUndoProperty && anEdit instanceof AbstractUndoablePropertyChange) {
            AbstractUndoablePropertyChange<PT, VT> anEdit2 = (AbstractUndoablePropertyChange<PT, VT>) anEdit;
            newValue = anEdit2.newValue;
            consolidatedCount++;
            return true;
        }
        return false;
    }
}
