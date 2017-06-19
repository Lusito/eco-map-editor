package info.lusito.mapeditor.editors.properties.nodes;

import info.lusito.mapeditor.editors.properties.PropertyTreeTableCell;
import info.lusito.mapeditor.editors.properties.api.PropertyInterface;
import info.lusito.mapeditor.editors.properties.converters.ConvertUtil;
import javafx.scene.control.ColorPicker;

// fixme: rgba and/or rgb?
public class PropertyTreeNodeColor extends PropertyTreeNode {

    protected final ColorPicker picker = new ColorPicker();

    public PropertyTreeNodeColor(PropertyInterface property) {
        super(property);
        
        picker.valueProperty().addListener((ov, o, n)-> applyValue(ConvertUtil.colorToString(picker.getValue())));
    }

    public void addToCell(PropertyTreeTableCell cell) {
        setValue(getValue(), false);
        cell.setControlGraphic(picker);
        cell.addFocusSelect(picker);
    }

    @Override
    public void setValue(String value, boolean apply) {
        disableApplyValue = true;
        if (value == null) {
            picker.setValue(ConvertUtil.toColor(getDefaultValue()));
            picker.setDisable(true);
        } else {
            picker.setValue(ConvertUtil.toColor(value));
            picker.setDisable(false);
        }
        disableApplyValue = false;

        super.setValue(value, apply);
    }

    @Override
    public void focusControl() {
        picker.requestFocus();
    }
}
