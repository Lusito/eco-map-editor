package info.lusito.mapeditor.editors.properties.nodes;

import info.lusito.mapeditor.editors.properties.PropertyTreeTableCell;
import info.lusito.mapeditor.editors.properties.api.PropertyInterface;

public class PropertyTreeNode {

    protected boolean disableApplyValue;
    private final String name;
    private final String description;
    protected final PropertyInterface property;

    public PropertyTreeNode(PropertyInterface property) {
        this.property = property;
        name = property.getName();
        description = property.getDescription();
    }

    public PropertyTreeNode(String name, String description) {
        property = null;
        this.name = name;
        this.description = description;
    }

    public void addToCell(PropertyTreeTableCell cell) {
        cell.setText(null);
        cell.setGraphic(null);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return property.getValue();
    }

    public void setValue(String value, boolean apply) {
        if (apply) {
            property.setValue(value);
        }
    }
    
    public void applyValue(String value) {
        if(!disableApplyValue) {
            property.setValue(value);
        }
    }

    public String getDefaultValue() {
        return property.getDefaultValue();
    }

    public PropertyInterface getProperty() {
        return property;
    }

    public void focusControl() {
    }

}
