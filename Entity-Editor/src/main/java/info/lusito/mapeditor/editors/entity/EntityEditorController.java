package info.lusito.mapeditor.editors.entity;

import info.lusito.mapeditor.common.CheckedFileDrop;
import info.lusito.mapeditor.common.AbstractController;
import info.lusito.mapeditor.common.EcoIcons;
import info.lusito.mapeditor.common.EcoPropertyFX;
import info.lusito.mapeditor.common.EcoPropertyEditorController;
import info.lusito.mapeditor.common.EcoPropertyUtil;
import info.lusito.mapeditor.common.dnd.DraggableTableRow;
import info.lusito.mapeditor.editors.properties.api.PropertyInterface;
import info.lusito.mapeditor.editors.properties.api.adapters.PropertiesAdapter;
import info.lusito.mapeditor.editors.properties.converters.ConvertUtil;
import info.lusito.mapeditor.persistence.component.EcoComponent;
import info.lusito.mapeditor.persistence.entity.EcoEntityComponent;
import info.lusito.mapeditor.persistence.entity.EcoEntity;
import info.lusito.mapeditor.projecttype.GameProject;
import info.lusito.mapeditor.service.filewatcher.FileWatcher;
import info.lusito.mapeditor.service.filewatcher.WatchedFile;
import info.lusito.mapeditor.service.filewatcher.picker.FilePicker;
import info.lusito.mapeditor.utils.ButtonCell;
import info.lusito.mapeditor.utils.UndoUtil;
import info.lusito.mapeditor.utils.undo.SimpleColorProperty;
import info.lusito.mapeditor.utils.undo.UndoableListAddRemove;
import info.lusito.mapeditor.utils.undo.UndoableListDrag;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import org.openide.filesystems.FileObject;

public class EntityEditorController extends AbstractController<EntityDataObject> implements Initializable {
    
    @FXML
    private TextField name;
    @FXML
    private TextField description;
    @FXML
    private TextField image;
    @FXML
    private TableView<EcoEntityComponentFX> componentTable;
    @FXML
    private TableColumn<EcoEntityComponentFX, String> componentNameColumn;
    @FXML
    private TableColumn<EcoEntityComponentFX, String> componentDeleteColumn;
    @FXML
    private TableView<EcoPropertyFX> metaTable;
    @FXML
    private TableColumn<EcoPropertyFX, String> metaKeyColumn;
    @FXML
    private TableColumn<EcoPropertyFX, String> metaValueColumn;
    @FXML
    private TableColumn<EcoPropertyFX, String> metaDeleteColumn;
    @FXML
    private TextField newMetaKey;
    @FXML
    private TextField newMetaValue;
    @FXML
    private Button addNewProperty;
    @FXML
    private ColorPicker shapeColor;
    
    ObservableList<EcoEntityComponentFX> components = FXCollections.observableArrayList();
    private final ObservableList<EcoPropertyFX> properties = FXCollections.observableArrayList();
    private EcoPropertyEditorController simplePropertyEditorController;
    private CheckedFileDrop imageFileDrop;
    private CheckedFileDrop componentFileDrop;
    private PropertiesAdapter propertiesEditor;
    private final SimpleColorProperty shapeColorProperty = new SimpleColorProperty(Color.CYAN);

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addNewProperty.disableProperty().bind(newMetaKey.textProperty().isEmpty());

        // component table
        componentTable.setRowFactory((tv) -> new DraggableTableRow(tv.hashCode(),this::moveComponentRow));
        double deleteWidth = componentDeleteColumn.prefWidthProperty().get();
        final DoubleBinding rest = componentTable.widthProperty().subtract(deleteWidth + 2 + 13);
        componentNameColumn.prefWidthProperty().bind(rest);
        
        componentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        componentDeleteColumn.setCellFactory((TableColumn<EcoEntityComponentFX, String> p) -> {
            return new ButtonCell<>(EcoIcons.DELETE, this::deleteComponent);
        });
        
        simplePropertyEditorController = new EcoPropertyEditorController(metaTable,
                metaKeyColumn,
                metaValueColumn,
                metaDeleteColumn,
                newMetaKey,
                newMetaValue,
                addNewProperty,
                this
        );
        componentTable.setItems(components);
        
        imageFileDrop = new CheckedFileDrop(image, this::getProject, this::onDropImage, false);
        imageFileDrop.addExtensions("png", "jpg", "jpeg", "tga");
        componentFileDrop = new CheckedFileDrop(componentTable, this::getProject, this::onDropComponent, false);
        componentFileDrop.addExtensions("xcd");
        
        shapeColorProperty.bindBidirectional(shapeColor.valueProperty());
    }
    
    @Override
    protected void onLoaded() {
        for (EcoEntityComponentFX component : components) {
            addPropertyListeners(component);
        }
        dataObject.setController(this);
    }
    
    @Override
    protected void load(final InputStream stream) throws IOException {
        EcoEntity ed = EcoEntity.load(stream);
        name.setText(ed.name == null ? "" : ed.name);
        description.setText(ed.description == null ? "" : ed.description);
        image.setText(ed.image == null ? "" : ed.image);
        shapeColor.setValue(ConvertUtil.toColor(ed.shapeColor, Color.CYAN));
        EcoPropertyUtil.load(ed.properties, properties);
        
        simplePropertyEditorController.setProperties(properties);
        
        UndoUtil.removeUndoRedo(
                name,
                description,
                image,
                newMetaKey,
                newMetaValue
        );
        
        name.textProperty().addListener(stringPropertyListener);
        description.textProperty().addListener(stringPropertyListener);
        image.textProperty().addListener(stringPropertyListener);
        shapeColorProperty.addListener(colorPropertyListener);
        
        if (ed.components != null) {
            GameProject project = (GameProject) getProject();
            FileWatcher fileWatcher = project.getFileWatcher("xcd");
            for (EcoEntityComponent ec : ed.components) {
                if (ec.src != null) {
                    WatchedFile file = fileWatcher.getFile(ec.src);
                    if (file != null) {
                        EcoComponent component = (EcoComponent) file.getContent();
                        components.add(new EcoEntityComponentFX(ec, component, false));
                    }
                }
            }
        }
    }
    
    @Override
    public void save() throws IOException {
        final EcoEntity ed = new EcoEntity();
        ed.name = name.textProperty().get();
        ed.description = description.textProperty().get();
        ed.properties = EcoPropertyUtil.save(properties);
        ed.image = image.getText();
        ed.shapeColor = ConvertUtil.colorToString(shapeColor.getValue());
        for (EcoEntityComponentFX component : components) {
            ed.components.add(getComponentDefinition(component));
        }
        
        FileObject fo = dataObject.getPrimaryFile();
        try (BufferedOutputStream out = new BufferedOutputStream(fo.getOutputStream())) {
            ed.save(out);
            markUnmodified();
        }
    }
    
    private void deleteComponent(EcoEntityComponentFX component) {
        if (components != null && !isPerformingUndoRedo()) {
            removePropertyListeners(component);
            setLastUndoProperty(null);
            int index = components.indexOf(component);
            components.remove(component);
            addUndoableEdit(new UndoableListAddRemove(this, components, component, index, false));
            updatePropertiesEditor();
        }
    }
    
    private boolean componentExists(WatchedFile file) {
        String src = file.getPath();
        for (EcoEntityComponentFX component : components) {
            if (src.equalsIgnoreCase(component.getSrc())) {
                return true;
            }
        }
        return false;
    }
    
    private void addComponent(WatchedFile file) {
        if (properties != null && !isPerformingUndoRedo()) {
            EcoComponent component = (EcoComponent) file.getContent();
            if (component != null && !componentExists(file)) {
                EcoEntityComponent ec = new EcoEntityComponent();
                ec.name = component.name;
                ec.src = file.getPath();
                EcoEntityComponentFX componentFX = new EcoEntityComponentFX(ec, component, true);
                addPropertyListeners(componentFX);
                
                setLastUndoProperty(null);
                int index = components.size();
                components.add(componentFX);
                addUndoableEdit(new UndoableListAddRemove(this, components, componentFX, index, true));
                updatePropertiesEditor();
            }
        }
    }
    
    private void moveComponentRow(int index, int insertBefore) {
        final int size = components.size();
        if (index < size && insertBefore < (size + 1) && index >= 0 && insertBefore >= 0) {
            EcoEntityComponentFX component = components.get(index);
            components.add(insertBefore, component);
            int remove = index;
            if (remove > insertBefore) {
                remove++;
            }
            components.remove(remove);
            addUndoableEdit(new UndoableListDrag(this, components, index, insertBefore));
            updatePropertiesEditor();
        }
    }
    
    private EcoEntityComponent getComponentDefinition(EcoEntityComponentFX prop) {
        EcoEntityComponent ec = new EcoEntityComponent();
        ec.name = prop.getName();
        ec.src = prop.getSrc();
        for (PropertyInterface property : prop.getProperties()) {
            ec.properties.put(property.getName(), property.getValue());
        }
        return ec;
    }
    
    @Override
    public void addPropertyListeners(Object o) {
        if (o instanceof EcoPropertyFX) {
            ((EcoPropertyFX) o).addListeners(stringPropertyListener);
        } else if (o instanceof EcoEntityComponentFX) {
            ((EcoEntityComponentFX) o).addListeners(stringPropertyListener, mapPropertyListener);
        }
    }
    
    @Override
    public void removePropertyListeners(Object o) {
        if (o instanceof EcoPropertyFX) {
            ((EcoPropertyFX) o).removeListeners(stringPropertyListener);
        } else if (o instanceof EcoEntityComponentFX) {
            ((EcoEntityComponentFX) o).removeListeners(stringPropertyListener, mapPropertyListener);
        }
    }
    
    @FXML
    private void onChooseImage(ActionEvent event) {
        FilePicker fp = new FilePicker();
        GameProject project = (GameProject) getProject();
        FileWatcher fileWatcher = project.getFileWatcher("*");
        fp.setAllowedExtension("png", "jpg", "jpeg", "tga");
        List<WatchedFile> files = fp.show(fileWatcher.getFiles(), "Select image");
        if (files != null && !files.isEmpty()) {
            for (WatchedFile file : files) {
                image.setText(file.getPath());
                break;
            }
        }
    }
    
    @FXML
    private void onAddComponent(ActionEvent event) {
        FilePicker fp = new FilePicker();
        GameProject project = (GameProject) getProject();
        FileWatcher fileWatcher = project.getFileWatcher("xcd");
        List<WatchedFile> files = fp.show(fileWatcher.getFiles(), "Select components to add");
        if (files != null && !files.isEmpty()) {
            for (WatchedFile file : files) {
                addComponent(file);
            }
        }
    }
    
    private void onDropImage(String relativePath) {
        image.setText(relativePath);
    }
    
    private void onDropComponent(String relativePath) {
        GameProject project = (GameProject) getProject();
        FileWatcher fileWatcher = project.getFileWatcher("xcd");
        WatchedFile watchedFile = fileWatcher.getFile(relativePath);
        if (watchedFile != null) {
            addComponent(watchedFile);
        } else {
            //fixme: inform user, that the file was not found
        }
    }
    
    public void setPropertiesEditor(PropertiesAdapter properties) {
        properties.loaded = true;
        this.propertiesEditor = properties;
        updatePropertiesEditor();
    }
    
    private void updatePropertiesEditor() {
        propertiesEditor.title = dataObject.getPrimaryFile().getNameExt();
        propertiesEditor.groups.clear();
        for (EcoEntityComponentFX component : components) {
            propertiesEditor.groups.add(component);
        }
        propertiesEditor.updateEverything();
    }
    
    @Override
    protected void afterRedo() {
        super.afterRedo();
        updatePropertiesEditor();
    }
    
    @Override
    protected void afterUndo() {
        super.afterUndo();
        updatePropertiesEditor();
    }
    
}
