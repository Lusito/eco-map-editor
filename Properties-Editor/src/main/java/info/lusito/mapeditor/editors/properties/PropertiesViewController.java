package info.lusito.mapeditor.editors.properties;

import info.lusito.mapeditor.editors.properties.api.PropertiesInterface;
import info.lusito.mapeditor.editors.properties.nodes.*;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.VBox;
import info.lusito.mapeditor.editors.properties.api.PropertyInterface;
import info.lusito.mapeditor.editors.properties.api.PropertiesGroupInterface;

/**
 * FXML Controller class
 */
public class PropertiesViewController implements Initializable {

    @FXML
    private VBox container;
    @FXML
    private TreeTableView<PropertyTreeNode> table;
    @FXML
    private TreeTableColumn<PropertyTreeNode, String> keyColumn;
    @FXML
    private TreeTableColumn<PropertyTreeNode, PropertyTreeNode> valueColumn;
    @FXML
    private TreeTableColumn<PropertyTreeNode, PropertyTreeNode> extraColumn;
    @FXML
    private Label description;
    private PropertiesInterface properties;
    private DoubleBinding rest;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        keyColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<PropertyTreeNode, String> param)
                -> new ReadOnlyStringWrapper(param.getValue().getValue().getName())
        );

        valueColumn.setCellFactory(list -> new PropertyTreeTableCell());
        valueColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<PropertyTreeNode, PropertyTreeNode> param)
                -> new SimpleObjectProperty(param.getValue().getValue())
        );
        extraColumn.setCellFactory(list -> new PropertyButtonTreeTableCell());
        extraColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<PropertyTreeNode, PropertyTreeNode> param)
                -> new SimpleObjectProperty(param.getValue().getValue())
        );

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                description.setText(newSelection.getValue().getDescription());
            } else {
                description.setText("Select an item to see its description");
            }
        });
    }

    PropertyTreeNode createNodeFor(PropertyInterface pd) {
        switch(pd.getType()) {
            case STRING:
                return new PropertyTreeNodeString(pd);
            case MULTILINE_STRING:
                return new PropertyTreeNodeMultilineString(pd);
            case FLOAT:
                return new PropertyTreeNodeFloat(pd);
            case INTEGER:
                return new PropertyTreeNodeInt(pd);
            case FILE:
                return new PropertyTreeNodeFile(pd,properties.getProject());
            case COLOR:
                return new PropertyTreeNodeColor(pd);
//            case "Entity Link":
//                return new PropertyTreeNodeString(pd.definition.name, pd.definition.description, pd.value);
            case SLIDER:
                return new PropertyTreeNodeSlider(pd);
            case BOOLEAN:
                return new PropertyTreeNodeBool(pd);
            case ENUM:
                return new PropertyTreeNodeEnum(pd);
        }
        return null;
    }
    
    void setExtraVisible(boolean visible) {
        if(rest == null || visible != extraColumn.isVisible()) {
            double sub = visible ? extraColumn.prefWidthProperty().get() : 0;
            rest = table.widthProperty().subtract(sub + 2 + 13).divide(2);
            keyColumn.prefWidthProperty().bind(rest);
            valueColumn.prefWidthProperty().bind(rest);
            extraColumn.setVisible(visible);
        }
    }
    
    void enable() {
        if (properties != null) {
            clear();
            setExtraVisible(properties.isInstance());
            final TreeItem<PropertyTreeNode> root = new TreeItem<>(new PropertyTreeNode("", ""));
            final ObservableList<TreeItem<PropertyTreeNode>> rootChildren = root.getChildren();
            for (PropertiesGroupInterface group : properties.getGroups()) {
                final TreeItem<PropertyTreeNode> groupItem
                        = new TreeItem<>(new PropertyTreeNode(group.getName(), group.getDescription()));
                final ObservableList<TreeItem<PropertyTreeNode>> compChildren = groupItem.getChildren();
                for (PropertyInterface pd : group.getProperties()) {
                    PropertyTreeNode node = createNodeFor(pd);
                    if(node != null)
                        compChildren.add(new TreeItem<>(node));
                }
                groupItem.setExpanded(true);
                rootChildren.add(groupItem);
            }

            table.setRoot(root);
            table.setShowRoot(false);
            container.setDisable(false);
        } else {
            System.out.println("null properties");
        }
    }

    void disable() {
        properties = null;
        clear();
        container.setDisable(true);
    }

    public boolean isEnabled() {
        return !container.isDisable();
    }

    void updateValues() {
        table.refresh();
    }

    public void setProperties(PropertiesInterface properties) {
        this.properties = properties;
        clear();
    }

    private void clear() {
        table.setRoot(null);
        description.setText("Select an item to see its description");
    }
}
