package info.lusito.mapeditor.utils.undo;

import javafx.collections.ObservableList;
import javax.swing.undo.CannotRedoException;

public class UndoableListAddRemove<T> extends AbstractUndoableEditFX {

    protected final ObservableList<T> items;
    protected final T item;
    protected final int index;
    protected final boolean add;

    public UndoableListAddRemove(UndoContext context, ObservableList<T> items, T item, int index, boolean add) {
        super(context);
        this.items = items;
        this.item = item;
        this.index = index;
        this.add = add;
    }

    protected void performAddRemove(boolean add) throws CannotRedoException {
        if (add) {
            items.add(index, item);
            context.addPropertyListeners(item);
        } else {
            items.remove(item);
            context.removePropertyListeners(item);
        }
    }

    @Override
    protected void performUndo() throws CannotRedoException {
        performAddRemove(!add);
    }

    @Override
    protected void performRedo() throws CannotRedoException {
        performAddRemove(add);
    }
}
