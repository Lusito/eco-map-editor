package info.lusito.mapeditor.utils.undo;

import javafx.application.Platform;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public abstract class AbstractUndoableEditFX extends AbstractUndoableEditBase {

    public AbstractUndoableEditFX(UndoContext context) {
        super(context);
    }

    @Override
    public final void undo() throws CannotUndoException {
        super.undo();
        context.beforeUndo(consolidatedCount);
        Platform.runLater(this::internalUndo);
    }

    @Override
    public final void redo() throws CannotRedoException {
        super.redo();
        context.beforeRedo(consolidatedCount);
        Platform.runLater(this::internalRedo);
    }
}
