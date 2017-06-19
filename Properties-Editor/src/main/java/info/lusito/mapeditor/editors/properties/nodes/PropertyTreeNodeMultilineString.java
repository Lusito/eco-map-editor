package info.lusito.mapeditor.editors.properties.nodes;

import info.lusito.mapeditor.editors.properties.PropertyTreeTableCell;
import info.lusito.mapeditor.editors.properties.api.PropertyInterface;
import info.lusito.mapeditor.utils.DialogUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;

public class PropertyTreeNodeMultilineString extends PropertyTreeNode {

    private final SimpleStringProperty textProperty = new SimpleStringProperty();
    protected final Button button = new Button("edit");

    public PropertyTreeNodeMultilineString(PropertyInterface property) {
        super(property);
        textProperty.addListener((ov, o, n) -> applyValue(n));
        button.setOnMouseClicked((e)-> {
            String result = DialogUtil.promptMultiline("Enter Value", textProperty.getValue());
            if(result != null) {
                textProperty.setValue(result);
            }
        });
    }

    public void addToCell(PropertyTreeTableCell cell) {
        setValue(getValue(), false);
        cell.setControlGraphic(button);
        cell.addFocusSelect(button);
    }

    @Override
    public void setValue(String value, boolean apply) {
        disableApplyValue = true;
        if (value == null) {
            textProperty.setValue("");
            button.setText("default");
            button.setDisable(true);
        } else {
            textProperty.setValue(value);
            button.setText("edit");
            button.setDisable(false);
        }
        disableApplyValue = false;

        super.setValue(value, apply);
    }

    @Override
    public void focusControl() {
        button.requestFocus();
    }
}
