package info.lusito.mapeditor.common;

import info.lusito.mapeditor.utils.ButtonCell;
import info.lusito.mapeditor.utils.UndoUtil;
import info.lusito.mapeditor.utils.undo.UndoableListAddRemove;
import info.lusito.mapeditor.utils.undo.UndoContext;
import java.util.Comparator;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class EcoPropertyEditorController {

    public static ComparePropertyFX comparator = new ComparePropertyFX();

    private final TableView<EcoPropertyFX> table;
    private final TextField newKey;
    private final TextField newValue;

    private ObservableList<EcoPropertyFX> properties;

    // For undo/redo support
    private final UndoContext undoContext;

    public EcoPropertyEditorController(TableView<EcoPropertyFX> table,
            TableColumn<EcoPropertyFX, String> keyColumn,
            TableColumn<EcoPropertyFX, String> valueColumn,
            TableColumn<EcoPropertyFX, String> deleteColumn,
            TextField newKey,
            TextField newValue,
            Button addButton,
            UndoContext undoContext) {
        this.table = table;
        this.newKey = newKey;
        this.newValue = newValue;
        this.undoContext = undoContext;
        addButton.disableProperty().bind(newKey.textProperty().isEmpty());
        addButton.setOnAction(this::onNewPropertyAdd);
        newKey.setOnKeyPressed(this::onNewPropertyKeyPressed);
        newValue.setOnKeyPressed(this::onNewPropertyKeyPressed);

        // meta table
        double delWidth = deleteColumn.prefWidthProperty().get() / 2;
        final DoubleBinding halfRest = table.widthProperty().divide(2).subtract(delWidth + 1 + 7);
        keyColumn.prefWidthProperty().bind(halfRest);
        valueColumn.prefWidthProperty().bind(halfRest);

        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        deleteColumn.setCellFactory((TableColumn<EcoPropertyFX, String> p) -> {
            return new ButtonCell<>(EcoIcons.DELETE, this::deleteProperty);
        });

        UndoUtil.removeUndoRedo(
                newKey,
                newValue
        );
    }

    public void setProperties(ObservableList<EcoPropertyFX> properties) {
        if (this.properties != null) {
            for (EcoPropertyFX property : this.properties) {
                undoContext.removePropertyListeners(property);
            }
        }
        this.properties = properties;
        table.setItems(new SortedList(properties, comparator));
        
        if(properties != null) {
            for (EcoPropertyFX property : properties) {
                undoContext.addPropertyListeners(property);
            }
        }
    }

    private EcoPropertyFX getProperty(String key) {
        for (EcoPropertyFX property : properties) {
            if (key.equalsIgnoreCase(property.getKey())) {
                return property;
            }
        }
        return null;
    }

    private void doAddNewProperty() {
        if (properties != null && !undoContext.isPerformingUndoRedo()) {
            String key = newKey.textProperty().get();
            if (!key.isEmpty()) {
                String value = newValue.textProperty().get();
                EcoPropertyFX property = getProperty(key);
                if (property != null) {
                    // if it already exists, modify the existing
                    property.setKey(key);
                    property.setValue(value);
                } else {
                    property = new EcoPropertyFX(key, value);
                    undoContext.addPropertyListeners(property);

                    undoContext.setLastUndoProperty(null);
                    int index = properties.size();
                    properties.add(property);
                    undoContext.addUndoableEdit(new UndoableListAddRemove(undoContext, properties, property, index, true));
                }
            }
        }
    }

    private void deleteProperty(EcoPropertyFX property) {
        if (properties != null && !undoContext.isPerformingUndoRedo()) {
            undoContext.removePropertyListeners(property);
            undoContext.setLastUndoProperty(null);
            int index = properties.indexOf(property);
            properties.remove(property);
            undoContext.addUndoableEdit(new UndoableListAddRemove(undoContext, properties, property, index, false));
        }
    }

    private void onNewPropertyAdd(ActionEvent event) {
        doAddNewProperty();
    }

    private void onNewPropertyKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            doAddNewProperty();
        }
    }

    private static class ComparePropertyFX implements Comparator<EcoPropertyFX> {

        @Override
        public int compare(EcoPropertyFX a, EcoPropertyFX b) {
            return a.getKey().compareToIgnoreCase(b.getKey());
        }
    }
}
