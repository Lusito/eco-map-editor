package info.lusito.mapeditor.editors.map.layerview;

import info.lusito.mapeditor.editors.map.model.MapInterface;
import info.lusito.mapeditor.editors.map.model.MapUpdateType;
import info.lusito.mapeditor.common.AbstractTopComponentFX;
import java.io.IOException;
import java.util.Collection;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = "LayerViewTopComponent",
        iconBase = "info/lusito/mapeditor/editors/map/layerview/icon.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "overview", openAtStartup = true)
@ActionID(category = "Window", id = "info.lusito.mapeditor.editors.map.layerview.LayerViewTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_LayerViewAction",
        preferredID = "LayerViewTopComponent"
)
@Messages({
    "CTL_LayerViewAction=Layers",
    "CTL_LayerViewTopComponent=Layers",
    "HINT_LayerViewTopComponent=All layers in the map"
})
public final class LayerViewTopComponent extends AbstractTopComponentFX<LayerViewController> implements LookupListener, MapInterface.Listener {

    private Lookup.Result<MapInterface> result;
    private MapInterface map;

    public LayerViewTopComponent() {
        setName(Bundle.CTL_LayerViewTopComponent());
        setToolTipText(Bundle.HINT_LayerViewTopComponent());
    }

    @Override
    protected FXMLLoader loadFXML() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("LayerView.fxml"));
        fxmlLoader.load();
        return fxmlLoader;
    }

    @Override
    protected void onSceneCreated() {
        controller.setMap(map);
    }

    @Override
    public void componentOpened() {
        result = Utilities.actionsGlobalContext().lookupResult(MapInterface.class);
        result.addLookupListener(this);
    }

    @Override
    public void componentClosed() {
        result.removeLookupListener(this);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends MapInterface> allEvents = result.allInstances();
        if (!allEvents.isEmpty()) {
            setMap(allEvents.iterator().next());
        }
    }

    private void setMap(MapInterface map) {
        if (this.map != map) {
            if (this.map != null) {
                this.map.removeListener(this);
            }
            this.map = map;
            if (map != null) {
                map.addListener(this);
            }
            if (controller != null) {
                Platform.runLater(() -> {
                    controller.setMap(map);
                    if (map != null && map.isLoaded()) {
                        controller.enable();
                    }
                });
            }
        }
    }

    @Override
    public void onUpdate(MapUpdateType type) {
        if(type == MapUpdateType.LAYERS)
            Platform.runLater(() -> controller.enable());
    }

    @Override
    public void onClose() {
        Platform.runLater(() -> controller.disable());
    }
}
