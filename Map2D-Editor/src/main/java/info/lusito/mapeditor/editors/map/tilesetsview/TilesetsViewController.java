package info.lusito.mapeditor.editors.map.tilesetsview;

import com.badlogic.gdx.Gdx;
import info.lusito.mapeditor.editors.map.model.MapInterface;
import info.lusito.mapeditor.editors.map.model.TilesetInterface;
import info.lusito.mapeditor.projecttype.GameProject;
import info.lusito.mapeditor.service.filewatcher.FileWatcher;
import info.lusito.mapeditor.service.filewatcher.WatchedFile;
import info.lusito.mapeditor.service.filewatcher.picker.FilePicker;
import info.lusito.mapeditor.utils.DialogUtil;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 */
public class TilesetsViewController implements Initializable {

    @FXML
    private Button addButton;
    @FXML
    private TabPane tabPane;
    @FXML
    private VBox container;
    MapInterface map;
    @FXML
    private ComboBox<Float> zoom;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        tabPane.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Tab> o, Tab oldValue, Tab newValue) -> {
            setFocusedTab(newValue);
        });
        ArrayList<Float> zoomItems = new ArrayList();
        zoomItems.add(0.25f);
        zoomItems.add(0.5f);
        zoomItems.add(1f);
        zoomItems.add(2f);
        zoomItems.add(4f);
        zoom.setItems(FXCollections.observableList(zoomItems));
        zoom.getSelectionModel().select(2);
        zoom.getSelectionModel().selectedItemProperty().addListener((o, oldValue, newValue) -> {
            final ObservableList<Tab> tabs = tabPane.getTabs();
            for (Tab tab : tabs) {
                TilesetTab tTab = (TilesetTab)tab;
                tTab.setZoom(newValue);
            }
        });
        disable();
    }

    void setMap(MapInterface map) {
        this.map = map;
        clear();
    }

    void enable() {
        if (map != null) {
            //fixme: remember old tabs, so they don't have to be recreated, if already existing
            final ObservableList<Tab> tabs = tabPane.getTabs();
            tabs.clear();
            TilesetInterface focusTileset = map.getFocusTileset();
            for (TilesetInterface tileset : map.getTilesets()) {
                TilesetTab tab = createTab(tileset);
                tabs.add(tab);
                if(tileset == focusTileset) {
                    tabPane.getSelectionModel().select(tab);
                }
            }
            System.out.println(map.getName());
        } else {
            System.out.println("null map");
        }
        container.setDisable(false);
    }

    void disable() {
        map = null;
        clear();
        container.setDisable(true);
    }

    private void clear() {
        tabPane.getTabs().clear();
    }

    private TilesetTab createTab(TilesetInterface tileset) {
        TilesetTab tab = new TilesetTab(tileset, this);
        tab.setZoom(zoom.getValue());
        tab.setOnCloseRequest((e)-> {
            boolean isUsed = tileset.isUsed();
            if(!isUsed || DialogUtil.confirm("Remove this tilset?",
                    "This tileset is in use. Do you want to remove it and all its references?", false)) {
                tileset.remove();
            } else {
                e.consume();
            }
        });
        return tab;
    }

    private void setFocusedTab(Tab newValue) {
        if(newValue != null) {
            TilesetTab tab = (TilesetTab)newValue;
            tab.onFocus();
        }
    }

    @FXML
    private void onAddTileset(ActionEvent event) {
        FilePicker fp = new FilePicker();
        GameProject project = map.getProject();
        FileWatcher fileWatcher = project.getFileWatcher("xtd");
        List<WatchedFile> files = fp.show(fileWatcher.getFiles(), "Select tilesets to add");
        if(files != null && !files.isEmpty()) {
            //fixme: find a better way than postRunnable
            Gdx.app.postRunnable(()-> {
                for (WatchedFile file : files) {
                    map.addTileset(file.getPath());
                }
            });
        }
    }
}
