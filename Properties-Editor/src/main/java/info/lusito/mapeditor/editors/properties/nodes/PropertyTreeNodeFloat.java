package info.lusito.mapeditor.editors.properties.nodes;

import info.lusito.mapeditor.editors.properties.converters.LimitedFloatStringConverter;
import info.lusito.mapeditor.editors.properties.PropertyTreeTableCell;
import info.lusito.mapeditor.editors.properties.api.PropertyInterface;
import javafx.scene.control.TextFormatter;

public class PropertyTreeNodeFloat extends PropertyTreeNodeString {

    private final LimitedFloatStringConverter converter;

    public PropertyTreeNodeFloat(PropertyInterface property) {
        super(property);
        converter = new LimitedFloatStringConverter("0", "0");
        textField.setTextFormatter(new TextFormatter(converter));
    }

    public void addToCell(PropertyTreeTableCell cell) {
        converter.setMinMax(property.getMinimum(), property.getMaximum());
        super.addToCell(cell);
    }
}
