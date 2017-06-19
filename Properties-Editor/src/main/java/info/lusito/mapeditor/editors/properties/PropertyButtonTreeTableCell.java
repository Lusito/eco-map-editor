package info.lusito.mapeditor.editors.properties;

import info.lusito.mapeditor.editors.properties.nodes.PropertyTreeNode;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TreeTableCell;

public class PropertyButtonTreeTableCell extends TreeTableCell<PropertyTreeNode, PropertyTreeNode> {

    private final Button button = new Button("-");

    public PropertyButtonTreeTableCell() {
        setEditable(false);
        button.setMaxWidth(Double.MAX_VALUE);
        getStyleClass().add("extra");
        button.setOnMouseClicked((e)-> {
            PropertyTreeNode item = getItem();
            if(item.getValue() == null) {
                item.setValue(item.getDefaultValue(), true);
                item.focusControl();
                button.setText("-");
            } else {
                item.setValue(null, true);
                button.setText("+");
            }
        });
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

        if (empty || item == null || item.getProperty() == null || item.getDefaultValue() == null) {
            setText(null);
            setGraphic(null);
        } else {
            setGraphic(button);
            addFocusSelect(button);
            button.setText(item.getValue() == null ? "+" : "-");
        }
    }

}
