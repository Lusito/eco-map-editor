package info.lusito.mapeditor.common.dnd;

import javafx.scene.control.ListCell;

public class DraggableListCell<T> extends ListCell<T> {

    private final DraggableRowSupport support;

    public DraggableListCell(int hashCode, DraggableRowSupport.MoveListener moveListener) {
        support = new DraggableRowSupport(hashCode, this, moveListener);
    }
}
