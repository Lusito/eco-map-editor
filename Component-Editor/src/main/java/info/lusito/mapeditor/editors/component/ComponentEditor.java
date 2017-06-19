package info.lusito.mapeditor.editors.component;

import info.lusito.mapeditor.common.AbstractEditorFX;
import info.lusito.mapeditor.editors.properties.api.adapters.PropertiesAdapter;
import info.lusito.mapeditor.projecttype.GameProject;
import java.awt.Image;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.util.ImageUtilities;

public final class ComponentEditor
        extends AbstractEditorFX<ComponentDataObject, ComponentEditorController> {

    private static final long serialVersionUID = 5106225435491631823L;

    private PropertiesAdapter properties;

    public ComponentEditor() {
    }

    public ComponentEditor(ComponentDataObject obj) {
        initialize(obj);
    }

    @Override
    protected void setupPanel() {
        super.setupPanel();
        properties = new PropertiesAdapter((GameProject) FileOwnerQuery.getOwner(dataObject.getPrimaryFile()));
    }

    @Override
    protected void initController() {
        controller.setDataObject(dataObject, manager);
        controller.setPropertiesPreview(properties);
    }

    @Override
    protected FXMLLoader loadFXML() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ComponentEditor.fxml"));
        fxmlLoader.load();
        return fxmlLoader;
    }

    @Override
    public void componentClosed() {
        properties.close();
        super.componentClosed();
    }

    @Override
    public void componentShowing() {
        content.add(properties);
        super.componentShowing();
    }

    @Override
    public void componentHidden() {
        content.remove(properties);
        super.componentHidden();
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage("info/lusito/mapeditor/editors/component/icon.png");
    }
}
