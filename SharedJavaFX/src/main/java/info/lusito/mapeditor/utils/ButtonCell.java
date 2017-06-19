package info.lusito.mapeditor.utils;

import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.image.ImageView;

public class ButtonCell<T, VT> extends TableCell<T, VT> {
    private final Button button;

    public ButtonCell(String iconPath, Consumer<T> consumer) {
        button = new Button("", new ImageView(iconPath));
        setAlignment(Pos.CENTER);
        button.setFocusTraversable(false);
        button.setOnMouseClicked((e) -> {
            consumer.accept((T) getTableRow().getItem());
        });
    }

    @Override
    protected void updateItem(VT item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : button);
    }
}
