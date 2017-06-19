package info.lusito.mapeditor.editors.map.model;

import info.lusito.mapeditor.persistence.map.EcoMapLayerType;
import info.lusito.mapeditor.projecttype.GameProject;
import java.util.List;
import org.openide.filesystems.FileObject;

public interface MapInterface {

    boolean isLoaded();

    String getName();

    GameProject getProject();
    
    FileObject getProjectDir();

    void setName(String value);

    void createLayer(EcoMapLayerType type, String name);

    List<LayerInterface> getLayers();

    LayerInterface getFocusLayer();
    
    void moveLayer(int index, int insertBefore);

    List<TilesetInterface> getTilesets();

    void addTileset(String filename);

    TilesetInterface getFocusTileset();

    void addListener(Listener listener);

    void removeListener(Listener listener);

    public static interface Listener {

        void onUpdate(MapUpdateType type);

        void onClose();
    }
}
