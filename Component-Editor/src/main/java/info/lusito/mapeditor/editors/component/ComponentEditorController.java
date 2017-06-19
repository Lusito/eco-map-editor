package info.lusito.mapeditor.editors.component;

import info.lusito.mapeditor.common.AbstractController;
import info.lusito.mapeditor.common.EcoIcons;
import info.lusito.mapeditor.common.dnd.DraggableTableRow;
import info.lusito.mapeditor.editors.properties.api.PropertyType;
import info.lusito.mapeditor.editors.properties.api.adapters.PropertiesAdapter;
import info.lusito.mapeditor.editors.properties.api.adapters.PropertiesGroupAdapter;
import info.lusito.mapeditor.persistence.component.EcoComponent;
import info.lusito.mapeditor.persistence.component.EcoComponentProperty;
import info.lusito.mapeditor.utils.ButtonCell;
import info.lusito.mapeditor.utils.UndoUtil;
import info.lusito.mapeditor.utils.undo.UndoableListAddRemove;
import info.lusito.mapeditor.utils.undo.UndoableListDrag;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javax.swing.undo.CannotRedoException;
import org.openide.filesystems.FileObject;

public class ComponentEditorController extends AbstractController<ComponentDataObject> implements Initializable {

    @FXML
    private TextField name;
    @FXML
    private TextField description;
    @FXML
    private TableView<EcoComponentPropertyFX> table;
    @FXML
    private TableColumn<EcoComponentPropertyFX, String> nameColumn;
    @FXML
    private TableColumn<EcoComponentPropertyFX, String> typeColumn;
    @FXML
    private TableColumn<EcoComponentPropertyFX, String> deleteColumn;
    @FXML
    private TextField newPropertyName;
    @FXML
    private Button addNewProperty;
    @FXML
    private TextField propertyName;
    @FXML
    private TextField propertyDescription;
    @FXML
    private CheckBox multipleValues;
    @FXML
    private TextField minimum;
    @FXML
    private TextField maximum;
    @FXML
    private ComboBox<PropertyType> propertyType;
    @FXML
    private TextArea values;

    private EcoComponentPropertyFX currentProperty;
    private final ObservableList<EcoComponentPropertyFX> properties = FXCollections.observableArrayList();
    private PropertiesAdapter propertiesPreview;
    private PropertiesGroupAdapter previewGroup;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addNewProperty.disableProperty().bind(newPropertyName.textProperty().isEmpty());

        // table
        table.setRowFactory((tv) -> new DraggableTableRow(tv.hashCode(),this::movePropertyRow));
        double delWidth = deleteColumn.prefWidthProperty().get() / 2;
        final DoubleBinding halfRest = table.widthProperty().divide(2).subtract(delWidth + 1 + 7);
        nameColumn.prefWidthProperty().bind(halfRest);
        typeColumn.prefWidthProperty().bind(halfRest);

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        deleteColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        deleteColumn.setCellFactory((TableColumn<EcoComponentPropertyFX, String> p) -> {
            return new ButtonCell<>(EcoIcons.DELETE, this::deleteProperty);
        });

        // property type
        propertyType.valueProperty().addListener((l) -> {
            updateEnabledProperties();
        });
        propertyType.setItems(FXCollections.observableArrayList(PropertyType.values()));
        updateEnabledProperties();
    }

    void setPropertiesPreview(PropertiesAdapter properties) {
        properties.loaded = true;
        this.propertiesPreview = properties;
        previewGroup = new PropertiesGroupAdapter();
        propertiesPreview.getGroups().add(previewGroup);
        updatePreview();
    }
    
    private void updatePreview() {
        propertiesPreview.title = dataObject.getPrimaryFile().getNameExt();
        previewGroup.name = name.getText();
        previewGroup.description = description.getText();
        previewGroup.properties.clear();
        for (EcoComponentPropertyFX property : properties) {
            previewGroup.properties.add(property);
        }
        propertiesPreview.updateEverything();
    }

    @Override
    protected void onLoaded() {
        table.setItems(properties);

        UndoUtil.removeUndoRedo(
                name,
                description,
                propertyName,
                propertyDescription,
                newPropertyName,
                minimum,
                maximum,
                values
        );

        // Add Listeners
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                setProperty(newSelection);
            }
        });

        name.textProperty().addListener(stringPropertyListener);
        description.textProperty().addListener(stringPropertyListener);
        for (EcoComponentPropertyFX prop : properties) {
            addPropertyListeners(prop);
        }

        dataObject.setController(this);
    }
    
    @Override
    protected void load(final InputStream stream) throws IOException {
        EcoComponent cd = EcoComponent.load(stream);
        name.setText(cd.name == null ? "" : cd.name);
        description.setText(cd.description == null ? "" : cd.description);
        for (EcoComponentProperty cpd : cd.properties) {
            properties.add(new EcoComponentPropertyFX(cpd));
        }
    }

    @Override
    public void save() throws IOException {
        final EcoComponent cd = new EcoComponent();
        cd.name = name.getText();
        cd.description = description.getText();
        for (EcoComponentPropertyFX property : properties) {
            cd.properties.add(getPropertyDefinition(property));
        }

        FileObject fo = dataObject.getPrimaryFile();
        try (BufferedOutputStream out = new BufferedOutputStream(fo.getOutputStream())) {
            cd.save(out);
            markUnmodified();
        }
        updatePreview();
    }

    private EcoComponentProperty getPropertyDefinition(EcoComponentPropertyFX prop) {
        EcoComponentProperty cpd = new EcoComponentProperty();
        cpd.name = prop.getName();
        cpd.description = prop.getDescription();
        cpd.type = prop.getType().name();
        cpd.minimum = prop.getMinimum();
        cpd.maximum = prop.getmaximum();
        cpd.multiple = prop.getMultiple();
        cpd.values = prop.getValues();

        if (cpd.description.isEmpty()) {
            cpd.description = null;
        }
        if (cpd.minimum.isEmpty()) {
            cpd.minimum = null;
        }
        if (cpd.maximum.isEmpty()) {
            cpd.maximum = null;
        }
        if (cpd.values.isEmpty()) {
            cpd.values = null;
        }
        return cpd;
    }

    @Override
    public void addPropertyListeners(Object o) {
        if(o instanceof EcoComponentPropertyFX)
            addPropertyListeners((EcoComponentPropertyFX)o);
    }

    @Override
    public void removePropertyListeners(Object o) {
        if(o instanceof EcoComponentPropertyFX)
            removePropertyListeners((EcoComponentPropertyFX)o);
    }

    private void addPropertyListeners(EcoComponentPropertyFX prop) {
        prop.addListeners(stringPropertyListener, booleanPropertyListener, enumPropertyListener);
    }

    private void removePropertyListeners(EcoComponentPropertyFX prop) {
        prop.removeListeners(stringPropertyListener, booleanPropertyListener, enumPropertyListener);
    }

    @FXML
    private void onNewPropertyAdd(ActionEvent event) {
        doAddNewProperty();
    }

    @FXML
    private void onNewPropertyNameKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            doAddNewProperty();
        }
    }

    private boolean propertyExists(String name) {
        for (EcoComponentPropertyFX property : properties) {
            if (name.equalsIgnoreCase(property.getName())) {
                return true;
            }
        }
        return false;
    }

    private void doAddNewProperty() {
        if (!performingUndoRedo) {
            String name = newPropertyName.textProperty().get();
            if (!name.isEmpty()) {
                if (propertyExists(name)) {
                    //fixme: inform the user
                    return;
                }
                EcoComponentPropertyFX property = new EcoComponentPropertyFX(name, "String");
                addPropertyListeners(property);

                lastUndoProperty = null;
                int index = properties.size();
                properties.add(property);
                if (currentProperty == null) {
                    setProperty(property);
                }
                addUndoableEdit(new UndoablePropertyListAddRemove(property, index, true));
                table.getSelectionModel().select(property);
                propertyType.requestFocus();
            }
        }
    }

    private void deleteProperty(EcoComponentPropertyFX property) {
        if (!performingUndoRedo) {
            removePropertyListeners(property);
            lastUndoProperty = null;
            int index = properties.indexOf(property);
            if (currentProperty == property) {
                setProperty(null);
            }
            properties.remove(property);
            addUndoableEdit(new UndoablePropertyListAddRemove(property, index, false));
        }
    }

    private void setProperty(EcoComponentPropertyFX property) {
        if (currentProperty != null) {
            currentProperty.nameProperty().unbindBidirectional(propertyName.textProperty());
            currentProperty.descriptionProperty().unbindBidirectional(propertyDescription.textProperty());
            currentProperty.typeProperty().unbindBidirectional(propertyType.valueProperty());
            currentProperty.multipleProperty().unbindBidirectional(multipleValues.selectedProperty());
            currentProperty.minimumProperty().unbindBidirectional(minimum.textProperty());
            currentProperty.maximumProperty().unbindBidirectional(maximum.textProperty());
            currentProperty.valuesProperty().unbindBidirectional(values.textProperty());
        }
        currentProperty = property;

        if (property == null) {
            propertyName.setText("");
            propertyDescription.setText("");
            propertyType.setValue(PropertyType.STRING);
            multipleValues.setSelected(false);
            minimum.setText("");
            maximum.setText("");
            values.setText("");
        } else {
            propertyName.setText(property.getName());
            propertyDescription.setText(property.getDescription());
            propertyType.setValue(property.getType());
            multipleValues.setSelected(property.getMultiple());
            minimum.setText(property.getMinimum());
            maximum.setText(property.getmaximum());
            values.setText(property.getValues());

            property.nameProperty().bindBidirectional(propertyName.textProperty());
            property.descriptionProperty().bindBidirectional(propertyDescription.textProperty());
            property.typeProperty().bindBidirectional(propertyType.valueProperty());
            property.multipleProperty().bindBidirectional(multipleValues.selectedProperty());
            property.minimumProperty().bindBidirectional(minimum.textProperty());
            property.maximumProperty().bindBidirectional(maximum.textProperty());
            property.valuesProperty().bindBidirectional(values.textProperty());
        }
        updateEnabledProperties();
    }

    private void updateEnabledProperties() {
        PropertyType type = propertyType.getValue();
        boolean disabled = currentProperty == null;
        propertyName.setDisable(disabled);
        propertyDescription.setDisable(disabled);
        propertyType.setDisable(disabled);
        boolean multipleType = (type == PropertyType.FILE);
//        boolean multipleDisabled = disabled || !multipleType;
//        multipleValues.setDisable(multipleDisabled);
        multipleValues.setDisable(true);
        boolean minMaxType = (type == PropertyType.FLOAT || type == PropertyType.INTEGER || type == PropertyType.SLIDER);
        boolean minMaxDisabled = disabled || !minMaxType;
        minimum.setDisable(minMaxDisabled);
        maximum.setDisable(minMaxDisabled);
        boolean valuesType = (type == PropertyType.ENUM);
        boolean valuesDisabled = disabled || !valuesType;
        values.setDisable(valuesDisabled);
    }
    
    private void movePropertyRow(int index, int insertBefore) {
        final int size = properties.size();
        if (index < size && insertBefore < (size + 1) && index >= 0 && insertBefore >= 0) {
            EcoComponentPropertyFX component = properties.get(index);
            properties.add(insertBefore, component);
            int remove = index;
            if (remove > insertBefore) {
                remove++;
            }
            properties.remove(remove);
            addUndoableEdit(new UndoableListDrag(this, properties, index, insertBefore));
        }
    }
    
    private class UndoablePropertyListAddRemove extends UndoableListAddRemove<EcoComponentPropertyFX> {

        public UndoablePropertyListAddRemove(EcoComponentPropertyFX item, int index, boolean add) {
            super(ComponentEditorController.this, properties, item, index, add);
        }

        @Override
        protected void performAddRemove(boolean add) throws CannotRedoException {
            super.performAddRemove(add);
            if (add && currentProperty == null) {
                setProperty(item);
            } else if (!add && currentProperty == item) {
                setProperty(null);
            }
        }

    }
}
