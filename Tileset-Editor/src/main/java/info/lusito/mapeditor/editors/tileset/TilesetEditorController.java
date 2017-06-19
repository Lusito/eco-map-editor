package info.lusito.mapeditor.editors.tileset;

import info.lusito.mapeditor.common.AbstractController;
import info.lusito.mapeditor.common.CheckedFileDrop;
import info.lusito.mapeditor.common.EcoIcons;
import info.lusito.mapeditor.common.EcoPropertyFX;
import info.lusito.mapeditor.common.EcoPropertyEditorController;
import info.lusito.mapeditor.common.EcoPropertyUtil;
import info.lusito.mapeditor.persistence.common.EcoCompressionType;
import info.lusito.mapeditor.persistence.tileset.EcoTileInfo;
import info.lusito.mapeditor.persistence.tileset.EcoTileset;
import info.lusito.mapeditor.persistence.common.EcoImageDefinition;
import info.lusito.mapeditor.persistence.common.EcoSize;
import info.lusito.mapeditor.persistence.tileset.EcoTerrain;
import info.lusito.mapeditor.projecttype.GameProject;
import info.lusito.mapeditor.projecttype.GameProjectUtil;
import info.lusito.mapeditor.service.filewatcher.FileWatcher;
import info.lusito.mapeditor.service.filewatcher.WatchedFile;
import info.lusito.mapeditor.service.filewatcher.picker.FilePicker;
import info.lusito.mapeditor.utils.ButtonCell;
import info.lusito.mapeditor.utils.UndoUtil;
import info.lusito.mapeditor.utils.undo.AbstractUndoableEditFX;
import info.lusito.mapeditor.utils.undo.UndoContext;
import info.lusito.mapeditor.utils.undo.UndoableListAddRemove;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javax.swing.undo.CannotRedoException;
import org.openide.filesystems.FileObject;

public class TilesetEditorController extends AbstractController<TilesetDataObject> implements Initializable {

    public static CompareTerrainFX terrainComparator = new CompareTerrainFX();

    @FXML
    private TextField name;
    @FXML
    private TextField image;
    @FXML
    TextField gridX;
    @FXML
    TextField gridY;
    @FXML
    TextField marginX;
    @FXML
    TextField marginY;
    @FXML
    TextField paddingX;
    @FXML
    TextField paddingY;
    @FXML
    TextField width;
    @FXML
    TextField height;
    @FXML
    private Label gridNoMatch;
    @FXML
    ColorPicker gridColor;
    @FXML
    private ScrollPane previewPanel;
    @FXML
    Canvas zoomCanvas;
    @FXML
    private Button globalProperties;
    @FXML
    private Label propertyEditorTitle;
    @FXML
    private TableView<EcoPropertyFX> propertyTable;
    @FXML
    private TableColumn<EcoPropertyFX, String> propertyKeyColumn;
    @FXML
    private TableColumn<EcoPropertyFX, String> propertyValueColumn;
    @FXML
    private TableColumn<EcoPropertyFX, String> propertyDeleteColumn;
    @FXML
    private Button addNewProperty;
    @FXML
    private TextField newPropertyKey;
    @FXML
    private TextField newPropertyValue;
    @FXML
    private TabPane tabPane;
    @FXML
    private TableView<TerrainRowFX> terrainTable;
    @FXML
    private TableColumn<TerrainRowFX, String> terrainNameColumn;
    @FXML
    private TableColumn<TerrainRowFX, String> terrainDeleteColumn;
    @FXML
    private TextField newTerrainName;
    @FXML
    private Button addTerrain;

    private final ObservableList<TerrainRowFX> terrains = FXCollections.observableArrayList();
    private final ObservableList<EcoPropertyFX> properties = FXCollections.observableArrayList();
    private final HashMap<String, EcoTileInfoFX> tileMap = new HashMap();
    private TilesetPreview preview;
    private EcoPropertyEditorController simplePropertyEditorController;
    final CalculatedTilesetData calculatedTilesetData = new CalculatedTilesetData();
    private CheckedFileDrop imageFileDrop;
    private EcoTileInfoFX preparedTileInfo;
    private InvalidationListener preparedTileInfoListener;
    private FileObject currentImageFO;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        preview = new TilesetPreview(previewPanel, this);
        gridColor.setValue(Color.MAGENTA);
        gridColor.getCustomColors().add(Color.MAGENTA);
        gridNoMatch.visibleProperty().bind(calculatedTilesetData.invalid);
        gridNoMatch.managedProperty().bind(calculatedTilesetData.invalid);

        simplePropertyEditorController = new EcoPropertyEditorController(propertyTable,
                propertyKeyColumn,
                propertyValueColumn,
                propertyDeleteColumn,
                newPropertyKey,
                newPropertyValue,
                addNewProperty,
                this
        );
        image.textProperty().addListener(this::onImageChanged);
        gridX.textProperty().addListener(this::onValueChanged);
        gridY.textProperty().addListener(this::onValueChanged);
        marginX.textProperty().addListener(this::onValueChanged);
        marginY.textProperty().addListener(this::onValueChanged);
        paddingX.textProperty().addListener(this::onValueChanged);
        paddingY.textProperty().addListener(this::onValueChanged);
        globalProperties.setVisible(false);
        imageFileDrop = new CheckedFileDrop(image, this::getProject, this::onDropImage, false);
        imageFileDrop.addExtensions("png", "jpg", "jpeg", "tga");

        tabPane.getSelectionModel().selectedIndexProperty()
                .addListener((obs, oldValue, newValue) -> {
                    preview.setPropertyMode(newValue.intValue() == 0);
                });

        // meta table
        double delWidth = terrainDeleteColumn.prefWidthProperty().get();
        final DoubleBinding rest = terrainTable.widthProperty()
                .subtract(delWidth + 1 + 14);
        terrainNameColumn.prefWidthProperty().bind(rest);

        terrainNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        terrainDeleteColumn.setCellFactory((TableColumn<TerrainRowFX, String> p) -> {
            return new ButtonCell<>(EcoIcons.DELETE, this::deleteTerrain);
        });
        addTerrain.disableProperty().bind(newTerrainName.textProperty().isEmpty());
        addTerrain.setOnAction(this::onNewTerrainAdd);
        newTerrainName.setOnKeyPressed(this::onNewTerrainKeyPressed);
        terrainTable.getSelectionModel().selectedItemProperty().addListener((ob,o,n)-> {
            preview.updateOverlayImage();
        });
    }

    private void onNewTerrainAdd(ActionEvent event) {
        doAddNewTerrain();
    }

    private void onNewTerrainKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            doAddNewTerrain();
        }
    }

    private void deleteTerrain(TerrainRowFX terrain) {
        if (properties != null && !isPerformingUndoRedo()) {
            removePropertyListeners(terrain);
            setLastUndoProperty(null);
            int index = properties.indexOf(terrain);
            terrains.remove(terrain);
            addUndoableEdit(new UndoableListAddRemove(this, terrains, terrain, index, false));
        }
    }

    private EcoTerrain getTerrain(TerrainRowFX terrainRow) throws IOException {
        EcoTerrain terrain = new EcoTerrain();
        terrain.name = terrainRow.getName();
        terrain.setData(EcoCompressionType.NONE, terrainRow.getQuarters());
        return terrain;
    }

    private TerrainRowFX getTerrain(String name) {
        for (TerrainRowFX terrain : terrains) {
            if (name.equalsIgnoreCase(terrain.getName())) {
                return terrain;
            }
        }
        return null;
    }

    private void doAddNewTerrain() {
        if (properties != null && !isPerformingUndoRedo()) {
            String name = newTerrainName.textProperty().get();
            if (!name.isEmpty()) {
                TerrainRowFX terrain = getTerrain(name);
                if (terrain != null) {
                    // if it already exists, ignore
                } else {
                    terrain = new TerrainRowFX(name);
                    addPropertyListeners(terrain);//fixme

                    setLastUndoProperty(null);
                    int index = terrains.size();
                    terrains.add(terrain);
                    addUndoableEdit(new UndoableListAddRemove(this, terrains, terrain, index, true));
                }
            }
        }
    }

    private void onImageChanged(ObservableValue<? extends String> o, String ov, String nv) {
        checkImageChanged();
    }

    private void onValueChanged(ObservableValue<? extends String> o, String ov, String nv) {
        final Image img = preview.getImage();
        if (img != null) {
            calculatedTilesetData.update(this, img);
        }
    }

    private void checkImageChanged() {
        FileObject projectDir = GameProjectUtil.getProjectDir(dataObject);
        FileObject imageFO = projectDir.getFileObject(image.getText());
        if (imageFO != currentImageFO) {
            setImage(imageFO);
        }
    }

    @Override
    protected void afterRedo() {
        super.afterRedo();
        checkImageChanged();
    }

    @Override
    protected void afterUndo() {
        super.afterUndo();
        checkImageChanged();
    }

    @Override
    protected void onLoaded() {
        UndoUtil.removeUndoRedo(
                name,
                image,
                gridX,
                gridY,
                marginX,
                marginY,
                paddingX,
                paddingY,
                newPropertyKey,
                newPropertyValue,
                newTerrainName
        );

        addStringPropertyListener(
                name,
                image,
                gridX,
                gridY,
                marginX,
                marginY,
                paddingX,
                paddingY
        );
        simplePropertyEditorController.setProperties(properties);

        dataObject.setController(this);
        terrainTable.setItems(new SortedList(terrains, terrainComparator));
    }

    @Override
    protected void load(InputStream stream) throws IOException {
        FileObject projectDir = GameProjectUtil.getProjectDir(dataObject);
        EcoTileset td = EcoTileset.load(stream);
        name.setText(td.name == null ? "" : td.name);

        if (td.grid != null) {
            gridX.setText("" + td.grid.x);
            gridY.setText("" + td.grid.y);
        }
        if (td.margin != null) {
            marginX.setText("" + td.margin.x);
            marginY.setText("" + td.margin.y);
        }
        if (td.padding != null) {
            paddingX.setText("" + td.padding.x);
            paddingY.setText("" + td.padding.y);
        }
        if (td.image != null) {
            width.setText("" + td.image.width);
            height.setText("" + td.image.height);
            if (td.image.src != null) {
                image.setText(td.image.src);
            }
        }

        EcoPropertyUtil.load(td.properties, properties);

        if (td.tiles != null) {
            for (EcoTileInfo tile : td.tiles) {
                if (tile.properties != null && !tile.properties.isEmpty()) {
                    String key = tile.x + "," + tile.y;
                    tileMap.put(key, new EcoTileInfoFX(tile.x, tile.y, tile.properties));
                }
            }
        }
        
        if(td.terrains != null) {
            for (EcoTerrain terrain : td.terrains) {
                terrains.add(new TerrainRowFX(terrain.name, terrain.getData(EcoCompressionType.NONE)));
            }
        }
    }

    private void addStringPropertyListener(TextField... texts) {
        for (TextField text : texts) {
            text.textProperty().addListener(stringPropertyListener);
        }
    }

    private void setImage(FileObject fo) {
        this.currentImageFO = fo;
        if (fo != null) {
            Image img = preview.setImage(fo.toURL().toString());
            width.setText("" + (int) img.getWidth());
            height.setText("" + (int) img.getHeight());
        } else {
            preview.clearImage();
            width.setText("");
            height.setText("");
        }
    }

    private void onDropImage(String relativePath) {
        image.setText(relativePath);
    }

    public EcoSize getSizeDefinition(TextField x, TextField y, boolean nullEmpty) {
        final String xt = x.getText();
        final String yt = y.getText();
        if (nullEmpty && xt.isEmpty() && yt.isEmpty()) {
            return null;
        }
        EcoSize size = new EcoSize();
        size.x = Integer.parseInt(xt);
        size.y = Integer.parseInt(yt);
        return size;
    }

    private EcoTileInfo getTileInfo(EcoTileInfoFX tile, EcoTileset tileset) {
        Map<String, String> tileProps = EcoPropertyUtil.save(tile.getProperties());
        if (tileProps != null) {
            EcoTileInfo tileInfo = new EcoTileInfo();
            tileInfo.tileset = tileset;
            tileInfo.x = tile.getX();
            tileInfo.y = tile.getY();
            tileInfo.properties = tileProps;
            tileInfo.updateCoords();
            return tileInfo;
        }
        return null;
    }

    @Override
    public void save() throws IOException {
        EcoTileset td = new EcoTileset();
        td.name = name.getText();
        td.grid = getSizeDefinition(gridX, gridY, true);
        td.margin = getSizeDefinition(marginX, marginY, false);
        td.padding = getSizeDefinition(paddingX, paddingY, false);
        String img = image.getText();
        if (img != null) {
            td.image = new EcoImageDefinition();
            td.image.src = img;
            td.image.width = Integer.parseInt(width.getText());
            td.image.height = Integer.parseInt(height.getText());
        }

        td.properties = EcoPropertyUtil.save(properties);

        if (!tileMap.isEmpty()) {
            td.tiles = new ArrayList();
            for (EcoTileInfoFX tile : tileMap.values()) {
                EcoTileInfo tileInfo = getTileInfo(tile, td);
                if (tileInfo != null) {
                    //fixme: only add if within bounds
                    td.tiles.add(tileInfo);
                }
            }
            td.tiles.sort((EcoTileInfo a, EcoTileInfo b) -> {
                if (a.x != b.x) {
                    return a.x - b.x;
                }
                return a.y - b.y;
            });
        }

        if(!terrains.isEmpty()) {
            td.terrains = new ArrayList();
            for (TerrainRowFX terrain : terrains) {
                td.terrains.add(getTerrain(terrain));
            }
        }

        FileObject fo = dataObject.getPrimaryFile();
        try (BufferedOutputStream out = new BufferedOutputStream(fo.getOutputStream())) {
            td.save(out);
            markUnmodified();
        }
    }

    @Override
    public void addPropertyListeners(Object o) {
        if (o instanceof EcoPropertyFX) {
            ((EcoPropertyFX) o).addListeners(stringPropertyListener);
        } else if (o instanceof EcoTileInfoFX) {
            ((EcoTileInfoFX) o).addListeners(stringPropertyListener);
        } else if (o instanceof TerrainRowFX) {
            ((TerrainRowFX) o).addListeners(stringPropertyListener);
        }
    }

    @Override
    public void removePropertyListeners(Object o) {
        if (o instanceof EcoPropertyFX) {
            ((EcoPropertyFX) o).removeListeners(stringPropertyListener);
        } else if (o instanceof EcoTileInfoFX) {
            ((EcoTileInfoFX) o).removeListeners(stringPropertyListener);
        } else if (o instanceof TerrainRowFX) {
            ((TerrainRowFX) o).removeListeners(stringPropertyListener);
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

    private EcoTileInfoFX getTileInfo(int x, int y) {
        final String key = x + "," + y;
        EcoTileInfoFX info = tileMap.get(key);
        if (info == null) {
            if (preparedTileInfo != null) {
                preparedTileInfo.getProperties().removeListener(preparedTileInfoListener);
                preparedTileInfo.setPos(x, y);
            } else {
                preparedTileInfo = new EcoTileInfoFX(x, y, null);
            }

            final ObservableList<EcoPropertyFX> infoProperties = preparedTileInfo.getProperties();
            preparedTileInfoListener = new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    infoProperties.removeListener(this);
                    tileMap.put(key, preparedTileInfo);
                    preparedTileInfo = null;
                }
            };
            infoProperties.addListener(preparedTileInfoListener);
            info = preparedTileInfo;
        }
        return info;
    }

    void onTileClicked(int tileX, int tileY) {
        System.out.println(tileX + "/" + tileY);
        EcoTileInfoFX tileInfo = getTileInfo(tileX, tileY);
        simplePropertyEditorController.setProperties(tileInfo.getProperties());
        globalProperties.setVisible(true);
        propertyEditorTitle.setText("Tile Properties (" + tileX + "," + tileY + ")");
        preview.select(tileX, tileY);
    }

    @FXML
    private void onShowGlobalProperties(ActionEvent event) {
        onDeselect();
    }

    void onDeselect() {
        globalProperties.setVisible(false);
        simplePropertyEditorController.setProperties(properties);
        propertyEditorTitle.setText("Tileset Properties");
        preview.deselect();
    }

    void addQuarter(int id) {
        TerrainRowFX item = terrainTable.getSelectionModel().getSelectedItem();
        if(item != null && !item.hasQuarter(id)) {
            TerrainRowFX previousTerrain = null;
            for (TerrainRowFX terrain : terrains) {
                if(terrain.hasQuarter(id)) {
                    terrain.removeQuarter(id);
                    previousTerrain = terrain;
                }
            }
            item.addQuarter(id);
            addUndoableTerrainQuarterChange(id, previousTerrain, item);
        }
    }

    void removeQuarter(int id) {
        TerrainRowFX item = terrainTable.getSelectionModel().getSelectedItem();
        if(item != null && item.hasQuarter(id)) {
            item.removeQuarter(id);
            addUndoableTerrainQuarterChange(id, item, null);
        }
    }
    
    void addUndoableTerrainQuarterChange(int id, TerrainRowFX from, TerrainRowFX to) {
        addUndoableEdit(new UndoableTerrainQuarterChange(this, id, from, to));
    }

    Set<Integer> getQuarters() {
        TerrainRowFX item = terrainTable.getSelectionModel().getSelectedItem();
        if(item != null) {
            return item.getQuarters();
        }
        return null;
    }
    
    

    private class UndoableTerrainQuarterChange extends AbstractUndoableEditFX {

        private final int id;
        private final TerrainRowFX from;
        private final TerrainRowFX to;

        public UndoableTerrainQuarterChange(UndoContext context,
                int id, TerrainRowFX from, TerrainRowFX to) {
            super(context);
            this.id = id;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            if(from != null) {
                from.addQuarter(id);
            }
            if(to != null) {
                to.removeQuarter(id);
            }
            preview.updateOverlayImage();
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            if(from != null) {
                from.removeQuarter(id);
            }
            if(to != null) {
                to.addQuarter(id);
            }
            preview.updateOverlayImage();
        }
    }

    private static class CompareTerrainFX implements Comparator<TerrainRowFX> {

        @Override
        public int compare(TerrainRowFX a, TerrainRowFX b) {
            return a.getName().compareToIgnoreCase(b.getName());
        }
    }
}
