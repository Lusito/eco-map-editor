package info.lusito.mapeditor.utils.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public abstract class AbstractUndoableEditEDT extends AbstractUndoableEditBase {

    public AbstractUndoableEditEDT(UndoContext context) {
        super(context);
    }

    @Override
    public final void undo() throws CannotUndoException {
        super.undo();
        context.beforeUndo(consolidatedCount);
        internalUndo();
    }

    @Override
    public final void redo() throws CannotRedoException {
        super.redo();
        context.beforeRedo(consolidatedCount);
        internalRedo();
    }
}
