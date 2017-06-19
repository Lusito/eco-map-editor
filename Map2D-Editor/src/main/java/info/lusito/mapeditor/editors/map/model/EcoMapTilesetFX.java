package info.lusito.mapeditor.editors.map.model;

import info.lusito.mapeditor.persistence.tileset.EcoTileset;

public class EcoMapTilesetFX implements TilesetInterface {

    private final String filename;
    private final EcoTileset tileset;
    private final EcoMapFX map;
    private int selectedTileX = -1;
    private int selectedTileY = -1;

    public EcoMapTilesetFX(String filename, EcoTileset tileset, EcoMapFX map) {
        this.filename = filename;
        this.tileset = tileset;
        this.map = map;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public String toString() {
        return tileset.name;
    }

    @Override
    public void focus() {
        map.focusTileset(this, false);
    }

    @Override
    public void remove() {
        map.removeTileset(this);
    }

    @Override
    public EcoTileset getTileset() {
        return tileset;
    }

    @Override
    public void selectTile(int x, int y) {
        selectedTileX = x;
        selectedTileY = y;
        map.selectTile(this, x, y);
    }

    @Override
    public int getSelectedTileX() {
        return selectedTileX;
    }

    @Override
    public int getSelectedTileY() {
        return selectedTileY;
    }

    @Override
    public boolean isUsed() {
        return map.isTilesetUsed(tileset);
    }
}
