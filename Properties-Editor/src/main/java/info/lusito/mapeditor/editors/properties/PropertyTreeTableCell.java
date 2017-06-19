package info.lusito.mapeditor.editors.properties;

import info.lusito.mapeditor.editors.properties.nodes.PropertyTreeNode;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeTableCell;
import javafx.scene.layout.Region;

public class PropertyTreeTableCell extends TreeTableCell<PropertyTreeNode, PropertyTreeNode> {
    public PropertyTreeTableCell() {
        setEditable(false);
    }
    
    public void setControlGraphic(Region control) {
        control.setMaxWidth(Double.MAX_VALUE);
        setGraphic(control);
    }
    
    public void addFocusSelect(Node ...nodes) {
        //fixme: remove again?
        final ChangeListener<Boolean> listener = (ov,o,newValue) -> {
            if(newValue) {
                getTreeTableView().getSelectionModel().select(getTreeTableRow().getIndex());
            }
        };
        for (Node node : nodes) {
            node.focusedProperty().addListener(listener);
        }
    }

    @Override
    public void updateItem(PropertyTreeNode item, boolean empty) {
        super.updateItem(item, empty);

        final ObservableList<String> styleClass = getTreeTableRow().getStyleClass();
        styleClass.remove("section");
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if(item.getClass() == PropertyTreeNode.class)
                styleClass.add("section");
            item.addToCell(this);
        }
    }

}
