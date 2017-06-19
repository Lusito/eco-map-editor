package info.lusito.mapeditor.editors.map.layerview;

import info.lusito.mapeditor.editors.map.model.LayerInterface;
import info.lusito.mapeditor.editors.map.model.MapInterface;
import info.lusito.mapeditor.utils.DialogUtil;
import info.lusito.mapeditor.persistence.map.EcoMapLayerType;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;

/**
 * FXML Controller class
 *
 * @author Santo Pfingsten
 */
public class LayerViewController implements Initializable {

    @FXML
    private VBox container;
    @FXML
    private Slider opacitySlider;
    @FXML
    private Label opacityLabel;
    @FXML
    private ListView<LayerInterface> listView;
    @FXML
    private MenuButton addButton;
    @FXML
    private Button duplicateButton;
    @FXML
    private Button deleteButton;
    private MapInterface map;
    private LayerInterface selectedLayer;
    private boolean isAutomaticFocus;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        opacityLabel.textProperty().bind(opacitySlider.valueProperty().asString("%.0f%%"));

        listView.setCellFactory(LayerListCell.forListView((item) -> {
            SimpleBooleanProperty observable = new SimpleBooleanProperty();
            observable.addListener((obs, wasSelected, isNowSelected) -> {
                item.setVisible(isNowSelected);
            });
            observable.setValue(item.isVisible());
            return observable;
        }, (item) -> {
            SimpleBooleanProperty observable = new SimpleBooleanProperty();
            observable.addListener((obs, wasSelected, isNowSelected) -> {
                item.setLocked(isNowSelected);
            });
            observable.setValue(item.isLocked());
            return observable;
        }, (index, insertBefore) -> {
            // need to invert indices, since the list is upside down
            final int size = listView.getItems().size();
            map.moveLayer((size-index)-1, (size-insertBefore));
        }));
        opacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (selectedLayer != null) {
                selectedLayer.setOpacity(newValue.floatValue() / 100);
            }
        });
        listView.setOnMouseClicked((e) -> {
            if (e.getClickCount() == 2 && selectedLayer != null) {
                String result = getLayerName(selectedLayer.getName());
                if (result != null) {
                    selectedLayer.setName(result);
                    ObservableList<LayerInterface> items = listView.getItems();
                    items.set(items.indexOf(selectedLayer), selectedLayer);
                }
            }
        });
        listView.setOnEditCommit((t) -> {
            listView.getItems().set(t.getIndex(), t.getNewValue());
            System.out.println("setOnEditCommit");
        });
        listView.setOnEditCancel((e) -> {
        });
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedLayer = null;
            if (newValue != null) {
                opacitySlider.setValue(newValue.getOpacity() * 100);
                if(!isAutomaticFocus) {
                    newValue.focus();
                    newValue.showProperties();
                }
            } else {
                // fixme: unfocus
            }
            selectedLayer = newValue;
            isAutomaticFocus = false;
        });
        disable();
    }

    void setMap(MapInterface map) {
        this.map = map;
        clear();
    }

    void enable() {
        if (map != null) {
            final ObservableList<LayerInterface> items = listView.getItems();
            items.clear();
            for (LayerInterface layer : map.getLayers()) {
                items.add(0, layer);
            }
            final LayerInterface focusLayer = map.getFocusLayer();
            if(focusLayer != null) {
                isAutomaticFocus = true;
                listView.getSelectionModel().select(focusLayer);
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
        listView.getItems().clear();
        opacitySlider.setValue(100);
    }

    private String getLayerName(String defaultValue) {
        return DialogUtil.prompt("Enter the name of the layer", "Name:", defaultValue);
    }
    
    private void createLayer(EcoMapLayerType type) {
        String name = getLayerName("");
        if(name != null) {
            map.createLayer(type, name);
        }
    }
    
    @FXML
    private void onAddTileLayer(ActionEvent event) {
        createLayer(EcoMapLayerType.TILE);
    }

    @FXML
    private void onAddEntityLayer(ActionEvent event) {
        createLayer(EcoMapLayerType.ENTITY);
    }

    @FXML
    private void onAddImageLayer(ActionEvent event) {
        createLayer(EcoMapLayerType.IMAGE);
    }

    @FXML
    private void onDuplicate(ActionEvent event) {
        if (selectedLayer != null) {
            selectedLayer.duplicate();
        }
    }

    @FXML
    private void onDelete(ActionEvent event) {
        if (selectedLayer != null) {
            selectedLayer.remove();
        }
    }

    @FXML
    private void onHighlight(ActionEvent event) {
        if (selectedLayer != null) {
            selectedLayer.highlight();
        }
    }

    @FXML
    private void onProperties(ActionEvent event) {
        if (selectedLayer != null) {
            selectedLayer.showProperties();
        }
    }

}
