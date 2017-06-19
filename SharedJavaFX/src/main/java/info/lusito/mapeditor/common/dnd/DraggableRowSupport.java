package info.lusito.mapeditor.common.dnd;

import java.io.Serializable;
import javafx.collections.ObservableList;
import javafx.scene.control.IndexedCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

public class DraggableRowSupport {

    private static final DataFormat MY_FORMAT = new DataFormat("dnd/DraggableRow");

    private final int hashCode;
    private final IndexedCell cell;
    private final MoveListener moveListener;

    public DraggableRowSupport(int hashCode, IndexedCell cell, MoveListener moveListener) {
        this.hashCode = hashCode;
        this.cell = cell;
        this.moveListener = moveListener;
        cell.setOnDragDetected(this::onDragDetected);
        cell.setOnDragEntered(this::onDragOver);
        cell.setOnDragOver(this::onDragOver);
        cell.setOnDragExited(this::onDragExited);
        cell.setOnDragDropped(this::onDragDropped);
    }

    private void onDragDetected(MouseEvent e) {
        if (cell.isEmpty()) {
            return;
        }
        Dragboard db = cell.startDragAndDrop(TransferMode.COPY_OR_MOVE);
        ClipboardContent cc = new ClipboardContent();
        cc.put(MY_FORMAT, new DragData(hashCode, cell.getIndex()));
        db.setContent(cc);
        e.consume();
    }

    private void onDragOver(DragEvent e) {
        if (!cell.isEmpty()) {
            Dragboard db = e.getDragboard();
            if (db.hasContent(MY_FORMAT)) {
                e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                applyClass(e);
                e.consume();
            }
        }
    }

    private void onDragExited(DragEvent event) {
        if (!cell.isEmpty()) {
            final ObservableList<String> styleClass = cell.getStyleClass();
            styleClass.remove("insert-below-row");
            styleClass.remove("insert-above-row");
            Dragboard db = event.getDragboard();
            if (db.hasContent(MY_FORMAT)) {
                event.consume();
            }
        }
    }

    private void onDragDropped(DragEvent e) {
        if (!cell.isEmpty()) {
            final ObservableList<String> styleClass = cell.getStyleClass();
            styleClass.remove("insert-below-row");
            styleClass.remove("insert-above-row");
            Dragboard db = e.getDragboard();
            if (db.hasContent(MY_FORMAT)) {
                DragData data = (DragData) db.getContent(MY_FORMAT);
                if (data.hashCode == hashCode) {
                    int insertBefore = cell.getIndex();
                    if (e.getY() > cell.getHeight() / 2) {
                        insertBefore += 1;
                    }
                    if (insertBefore != data.index && insertBefore != (data.index + 1)) {
                        moveListener.move(data.index, insertBefore);
                    }
                }
                e.setDropCompleted(true);
                e.consume();
            }
        }
    }

    private void applyClass(DragEvent e) {
        final ObservableList<String> styleClass = cell.getStyleClass();
        styleClass.remove("insert-below-row");
        styleClass.remove("insert-above-row");
        if (e.getY() < cell.getHeight() / 2) {
            styleClass.add("insert-above-row");
        } else {
            styleClass.add("insert-below-row");
        }
    }

    private static class DragData implements Serializable {

        private static final long serialVersionUID = -8298197262543389722L;
        public final int index;
        public final int hashCode;

        private DragData(int hashCode, int index) {
            super();
            this.hashCode = hashCode;
            this.index = index;
        }
    }

    public interface MoveListener {

        void move(int index, int insertBefore);
    }
}
