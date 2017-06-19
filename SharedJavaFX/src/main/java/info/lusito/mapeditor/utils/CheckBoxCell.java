package info.lusito.mapeditor.utils;

import java.util.function.BiConsumer;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;

public class CheckBoxCell<T> extends TableCell<T, Boolean> {
    private final CheckBox checkbox;
    private boolean updating;

    public CheckBoxCell(BiConsumer<T, Boolean> consumer) {
        checkbox = new CheckBox();
        setAlignment(Pos.CENTER);
        checkbox.setFocusTraversable(false);
        checkbox.selectedProperty().addListener((o)-> {
            if(!updating)
                consumer.accept((T) getTableRow().getItem(), checkbox.isSelected());
        });
    }

    @Override
    protected void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : checkbox);
        if(!empty) {
            updating = true;
            checkbox.setSelected(item);
            updating = false;
        }
    }
}
