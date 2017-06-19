package info.lusito.mapeditor.editors.entity;

import info.lusito.mapeditor.common.AbstractEditorFX;
import info.lusito.mapeditor.editors.properties.api.adapters.PropertiesAdapter;
import info.lusito.mapeditor.projecttype.GameProject;
import java.awt.Image;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.util.ImageUtilities;

public final class EntityEditor
        extends AbstractEditorFX<EntityDataObject, EntityEditorController> {

    private static final long serialVersionUID = -1807917838124852897L;

    private PropertiesAdapter properties;

    public EntityEditor() {
    }

    public EntityEditor(EntityDataObject obj) {
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
        controller.setPropertiesEditor(properties);
    }

    @Override
    protected FXMLLoader loadFXML() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("EntityEditor.fxml"));
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
        return ImageUtilities.loadImage("info/lusito/mapeditor/editors/entity/icon.png");
    }
}
