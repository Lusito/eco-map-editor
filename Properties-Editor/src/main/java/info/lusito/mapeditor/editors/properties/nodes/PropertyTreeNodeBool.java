package info.lusito.mapeditor.editors.properties.nodes;

import info.lusito.mapeditor.editors.properties.PropertyTreeTableCell;
import info.lusito.mapeditor.common.SwitchButton;
import info.lusito.mapeditor.editors.properties.api.PropertyInterface;

public class PropertyTreeNodeBool extends PropertyTreeNode {

    protected final SwitchButton button = new SwitchButton();

    public PropertyTreeNodeBool(PropertyInterface property) {
        super(property);
        button.checkedProperty().addListener((ov, o, n)-> applyValue(Boolean.toString(n)));
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
            button.setChecked(Boolean.valueOf(getDefaultValue()));
            button.setDisable(true);
        } else {
            button.setChecked(Boolean.valueOf(value));
            button.setDisable(false);
        }
        disableApplyValue = false;

        super.setValue(value, apply);
    }
}
