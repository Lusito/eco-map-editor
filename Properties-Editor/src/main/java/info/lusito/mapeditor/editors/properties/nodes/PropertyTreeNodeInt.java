package info.lusito.mapeditor.editors.properties.nodes;

import info.lusito.mapeditor.editors.properties.converters.LimitedIntegerStringConverter;
import info.lusito.mapeditor.editors.properties.PropertyTreeTableCell;
import info.lusito.mapeditor.editors.properties.api.PropertyInterface;
import javafx.scene.control.TextFormatter;

public class PropertyTreeNodeInt extends PropertyTreeNodeString {

    private final LimitedIntegerStringConverter converter;

    public PropertyTreeNodeInt(PropertyInterface property) {
        super(property);
        converter = new LimitedIntegerStringConverter("0", "0");
        textField.setTextFormatter(new TextFormatter(converter));
    }

    public void addToCell(PropertyTreeTableCell cell) {
        converter.setMinMax(property.getMinimum(), property.getMaximum());
        super.addToCell(cell);
    }
}
