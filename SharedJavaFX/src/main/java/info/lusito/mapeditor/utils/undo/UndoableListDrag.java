package info.lusito.mapeditor.utils.undo;

import javafx.collections.ObservableList;
import javax.swing.undo.CannotRedoException;

public class UndoableListDrag<T> extends AbstractUndoableEditFX {

    private final ObservableList<T> items;
    private final int index;
    private final int insertBefore;

    public UndoableListDrag(UndoContext context, ObservableList<T> items, int index, int insertBefore) {
        super(context);
        this.items = items;
        this.index = index;
        this.insertBefore = insertBefore;
    }

    private void performDrag(int index, int insertBefore) throws CannotRedoException {
        final int size = items.size();
        if (index < size && insertBefore < (size + 1) && index >= 0 && insertBefore >= 0) {
            T item = items.get(index);
            items.add(insertBefore, item);
            int remove = index;
            if (remove > insertBefore) {
                remove++;
            }
            items.remove(remove);
        }
    }

    @Override
    protected void performUndo() throws CannotRedoException {
        if (index < insertBefore) {
            performDrag(insertBefore - 1, index);
        } else {
            performDrag(insertBefore, index + 1);
        }
    }

    @Override
    protected void performRedo() throws CannotRedoException {
        performDrag(index, insertBefore);
    }
}
