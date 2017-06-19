package info.lusito.mapeditor.editors.properties.nodes;

import info.lusito.mapeditor.editors.properties.PropertyTreeTableCell;
import info.lusito.mapeditor.editors.properties.api.PropertyInterface;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;

public class PropertyTreeNodeEnum extends PropertyTreeNode {

    private final ComboBox<String> comboBox;

    public PropertyTreeNodeEnum(PropertyInterface property) {
        super(property);
        comboBox = new ComboBox(FXCollections.observableArrayList(property.getPossibleValues()));
        
        comboBox.valueProperty().addListener((ov, o, n)-> applyValue(n));
    }

    public void addToCell(PropertyTreeTableCell cell) {
        setValue(getValue(), false);
        final SingleSelectionModel selectionModel = comboBox.getSelectionModel();
        if(selectionModel.getSelectedIndex() == -1)
            selectionModel.select(0);

        cell.setControlGraphic(comboBox);
        cell.addFocusSelect(comboBox);
    }

    @Override
    public void setValue(String value, boolean apply) {
        disableApplyValue = true;
        if (value == null) {
            comboBox.setValue(getDefaultValue());
            comboBox.setDisable(true);
        } else {
            comboBox.setValue(value);
            comboBox.setDisable(false);
        }
        disableApplyValue = false;

        super.setValue(value, apply);
    }

    @Override
    public void focusControl() {
        comboBox.requestFocus();
    }
}
