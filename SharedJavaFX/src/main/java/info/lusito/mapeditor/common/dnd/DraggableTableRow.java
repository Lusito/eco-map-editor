package info.lusito.mapeditor.common.dnd;

import javafx.scene.control.TableRow;

public class DraggableTableRow<T> extends TableRow<T> {

    private final DraggableRowSupport support;

    public DraggableTableRow(int hashCode, DraggableRowSupport.MoveListener moveListener) {
        support = new DraggableRowSupport(hashCode, this, moveListener);
    }
}
