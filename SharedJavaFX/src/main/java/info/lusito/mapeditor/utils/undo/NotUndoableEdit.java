package info.lusito.mapeditor.utils.undo;

import javax.swing.undo.AbstractUndoableEdit;

/**
 * This is for edits which happen at load time to correct invalidated data for
 * example.
 */
public final class NotUndoableEdit extends AbstractUndoableEdit {

    @Override
    public boolean canRedo() {
        return false;
    }

    @Override
    public boolean canUndo() {
        return false;
    }
}
