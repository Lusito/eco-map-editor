package info.lusito.mapeditor.common;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public final class SwitchButton extends HBox {

    private final Label label = new Label();
    private SimpleBooleanProperty checked = new SimpleBooleanProperty(true);

    public SwitchButton() {
        label.setAlignment(Pos.CENTER);
        getStyleClass().add("switch-button");
        getChildren().add(label);
        label.prefWidthProperty().bind(widthProperty().divide(2));
        checked.addListener((ov, oldValue, newValue) -> applyChecked(newValue));

        checked.set(false);
        applyChecked(false);
        setOnMouseClicked((e) -> {
            checked.set(!checked.get());
            e.consume();
        });
    }

    private void applyChecked(boolean checked) {
        getStyleClass().remove("checked");
        if (checked) {
            label.setText("ON");
            getStyleClass().add("checked");
        } else {
            label.setText("OFF");
        }
    }

    public SimpleBooleanProperty checkedProperty() {
        return checked;
    }

    public void setChecked(boolean value) {
        checked.set(value);
    }

    public boolean isChecked() {
        return checked.get();
    }
}
