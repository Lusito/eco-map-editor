package info.lusito.mapeditor.editors.tileset;

import info.lusito.mapeditor.common.AbstractEditorFX;
import java.awt.Image;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import org.openide.util.ImageUtilities;

public final class TilesetEditor
        extends AbstractEditorFX<TilesetDataObject, TilesetEditorController> {

    private static final long serialVersionUID = -2216328729285967344L;

    public TilesetEditor() {
    }

    public TilesetEditor(TilesetDataObject obj) {
        initialize(obj);
    }

    @Override
    protected void initController() {
        controller.setDataObject(dataObject, manager);
    }

    @Override
    protected FXMLLoader loadFXML() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TilesetEditor.fxml"));
        fxmlLoader.load();
        return fxmlLoader;
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage("info/lusito/mapeditor/editors/tileset/icon.png");
    }
}
