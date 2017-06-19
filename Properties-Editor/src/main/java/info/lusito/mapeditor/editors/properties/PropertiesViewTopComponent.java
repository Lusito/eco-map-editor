package info.lusito.mapeditor.editors.properties;

import info.lusito.mapeditor.common.AbstractTopComponentFX;
import info.lusito.mapeditor.editors.properties.api.PropertiesInterface;
import info.lusito.mapeditor.utils.InvokeUtil;
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
        preferredID = "PropertiesViewTopComponent",
        iconBase = "info/lusito/mapeditor/editors/properties/icon.png",//Fixme
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "properties", openAtStartup = true)
@ActionID(category = "Window", id = "info.lusito.mapeditor.editors.properties.PropertiesViewTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_PropertiesViewAction",
        preferredID = "PropertiesViewTopComponent"
)
@Messages({
    "CTL_PropertiesViewAction=Properties",
    "CTL_PropertiesViewTopComponent=Properties",
    "HINT_PropertiesViewTopComponent=Properties Editor"
})
public final class PropertiesViewTopComponent extends AbstractTopComponentFX<PropertiesViewController> implements LookupListener, PropertiesInterface.Listener {

    private Lookup.Result<PropertiesInterface> result;
    private PropertiesInterface properties;

    public PropertiesViewTopComponent() {
        setName(Bundle.CTL_PropertiesViewTopComponent());
        setToolTipText(Bundle.HINT_PropertiesViewTopComponent());
    }

    @Override
    protected FXMLLoader loadFXML() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PropertiesView.fxml"));
        fxmlLoader.load();
        return fxmlLoader;
    }

    @Override
    protected void onSceneCreated() {
        doSetProperties();
    }

    private void doSetProperties() {
        controller.setProperties(properties);
        if (properties != null && properties.isLoaded()) {
            controller.enable();
        } else {
            controller.disable();
        }
    }

    @Override
    public void componentOpened() {
        result = Utilities.actionsGlobalContext().lookupResult(PropertiesInterface.class);
        result.addLookupListener(this);
    }

    @Override
    public void componentClosed() {
        result.removeLookupListener(this);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends PropertiesInterface> allEvents = result.allInstances();
        if (!allEvents.isEmpty()) {
            setProperties(allEvents.iterator().next());
        }
    }

    private void setProperties(PropertiesInterface properties) {
        if (this.properties != properties) {
            if (this.properties != null) {
                this.properties.removeListener(this);
            }
            this.properties = properties;
            if (properties != null) {
                properties.addListener(this);
            }
            if (controller != null) {
                Platform.runLater(this::doSetProperties);
            }
            updateTitle();
        } else {
            Platform.runLater(() -> {
                if(!controller.isEnabled())
                    doSetProperties();
            });
        }
    }

    private void updateTitle() {
        InvokeUtil.runOnEDT(this::updateTitleNow);
    }

    private void updateTitleNow() {
        String label = properties != null ? properties.getTitle() + " - Properties" : "Properties";
        setToolTipText(label);
        setDisplayName(label);
    }

    @Override
    public void onUpdateEverything() {
        Platform.runLater(this::doSetProperties);
        updateTitle();
    }

    @Override
    public void onUpdateValues() {
        Platform.runLater(() -> controller.updateValues());
        updateTitle();
    }

    @Override
    public void onClose() {
        properties = null;
        Platform.runLater(() -> controller.disable());
        updateTitle();
    }
}
