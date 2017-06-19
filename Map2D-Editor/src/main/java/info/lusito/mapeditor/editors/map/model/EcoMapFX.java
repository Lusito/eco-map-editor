package info.lusito.mapeditor.editors.map.model;

import com.badlogic.gdx.graphics.Texture;
import info.lusito.mapeditor.editors.map.MapEditorController;
import info.lusito.mapeditor.editors.map.utils.FocusMode;
import info.lusito.mapeditor.editors.map.utils.UndoableMapEdit;
import info.lusito.mapeditor.persistence.animation.EcoAnimation;
import info.lusito.mapeditor.persistence.common.EcoCompressionType;
import info.lusito.mapeditor.persistence.entity.EcoEntity;
import info.lusito.mapeditor.persistence.map.EcoEntityLayer;
import info.lusito.mapeditor.persistence.map.EcoImageLayer;
import info.lusito.mapeditor.persistence.map.EcoMap;
import info.lusito.mapeditor.persistence.map.EcoMapEntity;
import info.lusito.mapeditor.persistence.map.EcoMapImage;
import info.lusito.mapeditor.persistence.map.EcoMapLayer;
import info.lusito.mapeditor.persistence.map.EcoMapLayerType;
import info.lusito.mapeditor.persistence.map.EcoMapTilesetReference;
import info.lusito.mapeditor.persistence.map.EcoTileLayer;
import info.lusito.mapeditor.persistence.tileset.EcoTileInfo;
import info.lusito.mapeditor.persistence.tileset.EcoTileset;
import info.lusito.mapeditor.projecttype.GameProject;
import info.lusito.mapeditor.projecttype.GameProjectUtil;
import info.lusito.mapeditor.service.filewatcher.FileWatcher;
import info.lusito.mapeditor.service.filewatcher.WatchedFile;
import info.lusito.mapeditor.sharedlibgdx.SharedContextProvider;
import info.lusito.mapeditor.utils.DialogUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.undo.CannotRedoException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

public class EcoMapFX implements MapInterface {

    private EcoMap map;
    private final Set<MapInterface.Listener> listeners = ConcurrentHashMap.newKeySet();
    public final List<LayerInterface> layers = new CopyOnWriteArrayList();
    public final List<TilesetInterface> tilesets = new CopyOnWriteArrayList();
    private EcoMapLayerFX focusLayer;
    private EcoMapLayerFX highlightLayer;
    private final MapEditorController controller;
    private EcoTileInfo paintTile;
    private EcoMapTilesetFX focusTileset;
    public TerrainInfo terrainInfo = new TerrainInfo();

    public EcoMapFX(MapEditorController controller) {
        this.controller = controller;
    }

    public MapEditorController getController() {
        return controller;
    }

    @Override
    public boolean isLoaded() {
        return map != null;
    }

    @Override
    public GameProject getProject() {
        return (GameProject) controller.getProject();
    }

    @Override
    public FileObject getProjectDir() {
        return GameProjectUtil.getProjectDir(controller.getDataObject());
    }

    @Override
    public String getName() {
        return map.name;
    }

    @Override
    public void setName(String value) {
        map.name = value;
    }

    public TerrainInfo getTerrainInfo() {
        return terrainInfo;
    }

    @Override
    public void addListener(MapInterface.Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(MapInterface.Listener listener) {
        listeners.remove(listener);
    }

    public void load(final InputStream stream) throws IOException {
        map = EcoMap.load(stream);
        for (EcoMapTilesetReference ref : map.tilesets) {
            ref.tileset = loadTileset(ref.src);
        }
        if (!map.layers.isEmpty()) {
            for (EcoMapLayer layer : map.layers) {
                layer.onAfterRead(map);
                switch(layer.getType()) {
                    case TILE:
                        break;
                    case ENTITY:
                        EcoEntityLayer entityLayer = (EcoEntityLayer) layer;
                        final FileWatcher fileWatcher = getProject().getFileWatcher("xed");
                        for (EcoMapEntity entity : entityLayer.entities) {
                            WatchedFile xedFile = fileWatcher.getFile(entity.type);
                            EcoEntity ed = (EcoEntity) xedFile.getContent();
                            Texture texture = getTexture(ed.image);
                            entity.attachment = new EcoMapEntityFX(entity, ed, texture);
                        }
                        break;
                    case IMAGE:
                        EcoImageLayer imageLayer = (EcoImageLayer) layer;
                        for (EcoMapImage image : imageLayer.images) {
                            boolean isAnimation = image.filename.toLowerCase().endsWith(".xad");
                            //fixme: dispose texture
                            if(isAnimation) {
                                try(InputStream stream2 = GameProjectUtil.getInputStream(getProjectDir(), image.filename)) {
                                    EcoAnimation animation = EcoAnimation.load(stream2);
                                    Texture texture = getTexture(animation.image.src);
                                    image.attachment = new EcoMapImageFX(image, texture, animation);
                                } catch(IOException e) {
                                    DialogUtil.showException("Error: Animation file could not be loaded: " + image.filename, e);
                                }
                            } else {
                                Texture texture = getTexture(image.filename);
                                image.attachment = new EcoMapImageFX(image, texture);
                            }
                        }
                        break;
                }
            }
            for (EcoMapLayer layer : map.layers) {
                layers.add(new EcoMapLayerFX(layer, this));
            }
            focusLayer((EcoMapLayerFX) layers.get(layers.size() - 1), FocusMode.MAP);
        }
        update(MapUpdateType.LAYERS);
        update(MapUpdateType.TILESETS);
    }

    private EcoTileset loadTileset(String filename) throws IOException {
        final FileObject projectDirObj = GameProjectUtil.getProjectDir(controller.getDataObject());
        try (InputStream stream = GameProjectUtil.getInputStream(projectDirObj, filename)) {
            final EcoTileset tileset = EcoTileset.load(stream);

            File projectDir = FileUtil.toFile(projectDirObj);
            File file = new File(projectDir, tileset.image.src);
            SharedContextProvider scp = Lookup.getDefault().lookup(SharedContextProvider.class);
            //fixme: dispose texture
            tileset.attachment = scp.getTexture(file.getAbsolutePath());

            if(tileset.terrains != null) {
                terrainInfo.prepareTileset(tileset, EcoCompressionType.NONE);
                terrainInfo.addTileset(tileset);
            }
            //fixme: only add if gridsize matches
            tilesets.add(new EcoMapTilesetFX(filename, tileset, this));
            return tileset;
        }
    }

    private boolean addTilesetEx(String filename) throws IOException {
        if (tilesetExists(filename)) {
            return false;
        }
        final EcoMapTilesetReference ref = new EcoMapTilesetReference();
        ref.src = filename;
        ref.tileset = loadTileset(filename);
        map.tilesets.add(ref);
        EcoMapTilesetFX tilesetFX = (EcoMapTilesetFX)tilesets.get(tilesets.size()-1);
        focusTileset(tilesetFX, true);
        controller.addUndoableEdit(new UndoableTilesetAdd(controller, tilesetFX, ref));
        return true;
    }

    private boolean tilesetExists(String filename) {
        for (EcoMapTilesetReference tileset : map.tilesets) {
            if (tileset.src.equalsIgnoreCase(filename)) {
                return true;
            }
        }
        return false;
    }

    void update(MapUpdateType type) {
        for (MapInterface.Listener listener : listeners) {
            listener.onUpdate(type);
        }
    }

    public void close() {
        for (MapInterface.Listener listener : listeners) {
            listener.onClose();
        }
    }

    @Override
    public List<LayerInterface> getLayers() {
        return layers;
    }

    @Override
    public List<TilesetInterface> getTilesets() {
        return tilesets;
    }

    @Override
    public void createLayer(EcoMapLayerType type, String name) {
        EcoMapLayer layer = null;
        switch (type) {
            case TILE:
                layer = new EcoTileLayer();
                ((EcoTileLayer) layer).onAfterRead(map);
                break;
            case ENTITY:
                layer = new EcoEntityLayer();
                break;
            case IMAGE:
                layer = new EcoImageLayer();
                break;
        }
        if (layer != null) {
            layer.name = name;
            addLayer(layer, layers.size());
        }
    }

    private void addLayer(EcoMapLayer layer, int index) {
        final EcoMapLayerFX layerFX = new EcoMapLayerFX(layer, this);
        doAddLayer(layerFX, index);
        controller.addUndoableEdit(new UndoableLayerAdd(controller, index));
    }

    private void doAddLayer(final EcoMapLayerFX layerFX, int index) {
        map.layers.add(index, layerFX.getLayer());
        layers.add(index, layerFX);
        focusLayer(layerFX, FocusMode.LAYER);
        update(MapUpdateType.LAYERS);
    }

    void removeLayer(EcoMapLayerFX layer) {
        int index = layers.indexOf(layer);
        if (index >= 0) {
            doRemoveLayer(index);
            controller.addUndoableEdit(new UndoableLayerRemove(controller, index));
        }
    }

    private void doRemoveLayer(int index) {
        layers.remove(index);
        map.layers.remove(index);
        if (!map.layers.isEmpty()) {
            focusLayer((EcoMapLayerFX) layers.get(index > 0 ? (index - 1) : 0), FocusMode.LAYER);
        }
        
        update(MapUpdateType.LAYERS);
    }

    void focusLayer(EcoMapLayerFX layer, FocusMode mode) {
        focusLayer = layer;
        controller.setFocusLayer(layer, mode);
    }

    public void refocusLayer(EcoMapLayerFX layerFX, FocusMode mode) {
        focusLayer(layerFX, mode);
        update(MapUpdateType.LAYERS);
    }

    public EcoMapLayerFX getFocusLayerFX() {
        return focusLayer;
    }

    @Override
    public LayerInterface getFocusLayer() {
        return focusLayer;
    }

    @Override
    public void moveLayer(int index, int insertBefore) {
        final int size = layers.size();
        if (index < size && insertBefore < (size + 1) && index >= 0 && insertBefore >= 0) {
            doMoveLayer(index, insertBefore);
            controller.addUndoableEdit(new UndoableLayerMove(controller, index, insertBefore));
        }
    }

    private void doMoveLayer(int index, int insertBefore) {
        EcoMapLayer mapLayer = map.layers.get(index);
        LayerInterface layer = layers.get(index);
        map.layers.add(insertBefore, mapLayer);
        layers.add(insertBefore, layer);
        int remove = index;
        if (remove > insertBefore) {
            remove++;
        }
        map.layers.remove(remove);
        layers.remove(remove);
    }

    void highlightLayer(EcoMapLayerFX layer) {
        highlightLayer = layer;
        //fixme: do stuff
    }

    public EcoMapLayerFX getHighlightLayer() {
        return highlightLayer;
    }
    
    EcoEntityLayer duplicateEntityLayer(EcoEntityLayer layer) {
        EcoEntityLayer newLayer = new EcoEntityLayer(layer);
        for (int i = 0; i < layer.entities.size(); i++) {
            final EcoMapEntity entity = layer.entities.get(i);
            newLayer.entities.get(i).attachment = new EcoMapEntityFX(entity, ((EcoMapEntityFX)entity.attachment));
        }
        return newLayer;
    }
    
    EcoImageLayer duplicateImageLayer(EcoImageLayer layer) {
        EcoImageLayer newLayer = new EcoImageLayer(layer);
        for (int i = 0; i < layer.images.size(); i++) {
            final EcoMapImage image = layer.images.get(i);
            newLayer.images.get(i).attachment = new EcoMapImageFX(image, ((EcoMapImageFX)image.attachment));
        }
        return newLayer;
    }

    void duplicateLayer(EcoMapLayerFX layer) {
        int index = layers.indexOf(layer);
        if (index >= 0) {
            EcoMapLayer newLayer = null;
            switch (layer.getType()) {
                case TILE:
                    newLayer = new EcoTileLayer((EcoTileLayer) layer.getLayer());
                    break;
                case ENTITY:
                    newLayer = duplicateEntityLayer((EcoEntityLayer) layer.getLayer());
                    break;
                case IMAGE:
                    newLayer = duplicateImageLayer((EcoImageLayer) layer.getLayer());
                    break;
            }
            if (newLayer != null) {
                if (newLayer.name != null) {
                    newLayer.name += " (copy)";
                }
                addLayer(newLayer, index+1);
            }
        }
    }

    public EcoMap getMap() {
        return map;
    }

    @Override
    public void addTileset(String filename) {
        try {
            addTilesetEx(filename);
        } catch (IOException e) {
            DialogUtil.showException("Error: Tileset file could not be loaded: " + filename, e);
        }
    }

    @Override
    public TilesetInterface getFocusTileset() {
        return focusTileset;
    }

    void focusTileset(EcoMapTilesetFX tilesetFX, boolean update) {
        if(this.focusTileset != tilesetFX) {
            this.focusTileset = tilesetFX;
            if (tilesetFX == null) {
                selectTile(null, -1, -1);
            } else {
                selectTile(tilesetFX, tilesetFX.getSelectedTileX(), tilesetFX.getSelectedTileY());
            }
            if (update) {
                update(MapUpdateType.TILESETS);
            }
        }
    }
    
    private void focusTilesetByIndex(int index) {
        if (tilesets.isEmpty()) {
            focusTileset(null, true);
        } else {
            if (index >= tilesets.size()) {
                index--;
            }
            focusTileset((EcoMapTilesetFX) tilesets.get(index), true);
        }
    }

    void removeTileset(EcoMapTilesetFX tilesetFX) {
        EcoTileset tileset = tilesetFX.getTileset();
        
        // Remove used tiles and create backups for them
        ArrayList<TilesetLayerBackup> backups = new ArrayList();
        EcoTileInfo[][] backup = null;
        for (EcoMapLayer layer : map.layers) {
            if(layer.getType() == EcoMapLayerType.TILE) {
                EcoTileLayer tileLayer = (EcoTileLayer)layer;

                //fixme: determine if layer contains tileset before this step
                boolean found = false;
                if(backup == null) {
                    backup = new EcoTileInfo[map.size.x][map.size.y];
                }
                for (int y = 0; y < map.size.y; y++) {
                    for (int x = 0; x < map.size.x; x++) {
                        EcoTileInfo ti = tileLayer.tiles[x][y];
                        if(ti != null && ti.tileset == tileset) {
                            backup[x][y] = ti;
                            tileLayer.tiles[x][y] = null;
                            found = true;
                        }
                    }
                }
                if(found) {
                    backups.add(new TilesetLayerBackup(tileLayer, backup));
                    backup = null;
                }
            }
        }
        
        // Remove the tileset from map and tilesets list
        int index = tilesets.indexOf(tilesetFX);
        EcoMapTilesetReference ref = map.tilesets.get(index);
        map.tilesets.remove(index);
        tilesets.remove(index);
        focusTilesetByIndex(index);
        terrainInfo.removeTileset(tileset);
        
        controller.addUndoableEdit(new UndoableTilesetRemove(controller, tilesetFX, ref, backups));
    }
    
    EcoMapTilesetReference getTilesetRef(EcoTileset tileset) {
        for (EcoMapTilesetReference ref : map.tilesets) {
            if(ref.tileset == tileset) {
                return ref;
            }
        }
        return null;
    }

    public EcoTileInfo getPaintTile() {
        return paintTile;
    }

    void selectTile(EcoMapTilesetFX tilesetFX, int x, int y) {
        if(tilesetFX != null && x >= 0 && y >= 0) {
            paintTile = tilesetFX.getTileset().getTileInfo(x, y, true);
        } else {
            paintTile = null;
        }
        controller.onTileSelected();
    }

    void requestRendering() {
        controller.requestRendering();
    }

    public void applyFocusLayer() {
        focusLayer(focusLayer, FocusMode.MAP);
    }

    public Texture getTexture(String relativePath) {
        SharedContextProvider scp = Lookup.getDefault().lookup(SharedContextProvider.class);
        //fixme: dispose texture
        File file = new File(FileUtil.toFile(getProjectDir()), relativePath);
        return scp.getTexture(file.getAbsolutePath());
    }

    boolean isTilesetUsed(EcoTileset tileset) {
        //Fixme: can this be done more efficiently?
        for (EcoMapLayer layer : map.layers) {
            if(layer.getType() == EcoMapLayerType.TILE) {
                EcoTileLayer tileLayer = (EcoTileLayer)layer;

                for (int y = 0; y < map.size.y; y++) {
                    for (int x = 0; x < map.size.x; x++) {
                        EcoTileInfo ti = tileLayer.tiles[x][y];
                        if(ti != null && ti.tileset == tileset) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private class UndoableLayerMove extends UndoableMapEdit {

        private final int index;
        private final int insertBefore;

        public UndoableLayerMove(MapEditorController controller, int index, int insertBefore) {
            super(controller);
            this.index = index;
            this.insertBefore = insertBefore;
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            if (index > insertBefore) {
                doMoveLayer(insertBefore, index+1);
            } else {
                doMoveLayer(insertBefore-1, index);
            }
            applyLayerFocus(FocusMode.LAYER);
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            doMoveLayer(index, insertBefore);
            applyLayerFocus(FocusMode.LAYER);
        }
    }

    private class UndoableLayerAdd extends UndoableMapEdit {

        private final int index;

        public UndoableLayerAdd(MapEditorController controller, int index) {
            super(controller);
            this.index = index;
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            doRemoveLayer(layers.indexOf(focusLayerFX));
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            doAddLayer(focusLayerFX, index);
        }
    }

    private class UndoableLayerRemove extends UndoableMapEdit {

        private final int index;

        public UndoableLayerRemove(MapEditorController controller, int index) {
            super(controller);
            this.index = index;
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            doAddLayer(focusLayerFX, index);
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            doRemoveLayer(layers.indexOf(focusLayerFX));
        }
    }
    private class UndoableTilesetAdd extends UndoableMapEdit {

        private final EcoMapTilesetFX tilesetFX;
        private final EcoMapTilesetReference ref;

        public UndoableTilesetAdd(MapEditorController controller, EcoMapTilesetFX tilesetFX, EcoMapTilesetReference ref) {
            super(controller);
            this.tilesetFX = tilesetFX;
            this.ref = ref;
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            int index = tilesets.indexOf(tilesetFX);
            map.tilesets.remove(ref);
            tilesets.remove(tilesetFX);
            focusTilesetByIndex(index);
            terrainInfo.removeTileset(tilesetFX.getTileset());
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            map.tilesets.add(ref);
            tilesets.add(tilesetFX);
            focusTilesetByIndex(tilesets.size()-1);
            terrainInfo.addTileset(tilesetFX.getTileset());
        }
    }

    private static class TilesetLayerBackup {
        private final EcoTileLayer layer;
        private final EcoTileInfo[][] tiles;

        public TilesetLayerBackup(EcoTileLayer layer, EcoTileInfo[][] tiles) {
            this.layer = layer;
            this.tiles = tiles;
        }
        
    }
    private class UndoableTilesetRemove extends UndoableMapEdit {

        private final EcoMapTilesetFX tilesetFX;
        private final EcoMapTilesetReference ref;
        private final List<TilesetLayerBackup> backups;

        public UndoableTilesetRemove(MapEditorController controller, EcoMapTilesetFX tilesetFX,
                EcoMapTilesetReference ref, List<TilesetLayerBackup> backups) {
            super(controller);
            this.tilesetFX = tilesetFX;
            this.ref = ref;
            this.backups = backups;
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            for (TilesetLayerBackup backup : backups) {
                for (int y = 0; y < map.size.y; y++) {
                    for (int x = 0; x < map.size.x; x++) {
                        if(backup.tiles[x][y] != null) {
                            backup.layer.tiles[x][y] = backup.tiles[x][y];
                        }
                    }
                }
            }
            map.tilesets.add(ref);
            tilesets.add(tilesetFX);
            focusTilesetByIndex(tilesets.size()-1);
            terrainInfo.addTileset(tilesetFX.getTileset());
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            for (TilesetLayerBackup backup : backups) {
                for (int y = 0; y < map.size.y; y++) {
                    for (int x = 0; x < map.size.x; x++) {
                        if(backup.tiles[x][y] != null) {
                            backup.layer.tiles[x][y] = null;
                        }
                    }
                }
            }
            int index = tilesets.indexOf(tilesetFX);
            map.tilesets.remove(ref);
            tilesets.remove(tilesetFX);
            focusTilesetByIndex(index);
            terrainInfo.removeTileset(tilesetFX.getTileset());
        }
    }
}
