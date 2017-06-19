package info.lusito.mapeditor.editors.map.tilesetsview;

import info.lusito.mapeditor.editors.map.model.TilesetInterface;
import info.lusito.mapeditor.persistence.tileset.EcoTileset;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import org.openide.filesystems.FileObject;

public class TilesetTab extends Tab {

    private final TilesetInterface tileset;
    private final TilesetSelector tilesetSelector;

    TilesetTab(TilesetInterface tileset, TilesetsViewController controller) {
        this.tileset = tileset;
        EcoTileset td = tileset.getTileset();
        setText(td.name);
        setTooltip(new Tooltip(tileset.getFilename()));
        
        ScrollPane scrollPane = new ScrollPane();
        setContent(scrollPane);
        final FileObject projectDir = controller.map.getProjectDir();
        
        FileObject imageFO = projectDir.getFileObject(td.image.src);
        tilesetSelector = new TilesetSelector(scrollPane, controller, tileset);
        if(imageFO != null)
            tilesetSelector.setImage(imageFO.toURL().toString());
    }

    void onFocus() {
        tileset.focus();
    }

    public String getName() {
        return tileset.getTileset().name;
    }

    public void setZoom(float zoom) {
        tilesetSelector.setZoom(zoom);
    }
}
