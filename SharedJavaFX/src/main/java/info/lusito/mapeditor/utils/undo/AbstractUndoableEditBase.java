package info.lusito.mapeditor.utils.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;

public abstract class AbstractUndoableEditBase extends AbstractUndoableEdit {

    protected final UndoContext context;
    protected int consolidatedCount = 1;

    public AbstractUndoableEditBase(UndoContext context) {
        this.context = context;
    }

    public final void internalUndo() {
        try {
            performUndo();
        } finally {
            context.afterUndo();
        }
    }

    protected abstract void performUndo() throws CannotRedoException;

    public final void internalRedo() {
        try {
            performRedo();
        } finally {
            context.afterRedo();
        }
    }

    protected abstract void performRedo() throws CannotRedoException;
}
