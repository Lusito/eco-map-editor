package info.lusito.mapeditor.editors.properties.nodes;

import info.lusito.mapeditor.editors.properties.PropertyTreeTableCell;
import info.lusito.mapeditor.editors.properties.api.PropertyInterface;
import javafx.scene.control.TextField;

public class PropertyTreeNodeString extends PropertyTreeNode {
    protected final TextField textField = new TextField();
    
    public PropertyTreeNodeString(PropertyInterface property) {
        super(property);
        textField.textProperty().addListener((ov, o, n)-> applyValue(n));
    }
    
    public void addToCell(PropertyTreeTableCell cell) {
        setValue(getValue(), false);
        cell.setControlGraphic(textField);
        cell.addFocusSelect(textField);
    }

    @Override
    public void setValue(String value, boolean apply) {
        disableApplyValue = true;
        if (value == null) {
            textField.setText(getDefaultValue());
            textField.setDisable(true);
        } else {
            textField.setText(value);
            textField.setDisable(false);
        }
        disableApplyValue = false;

        super.setValue(value, apply);
    }

    @Override
    public void focusControl() {
        textField.requestFocus();
    }
}
