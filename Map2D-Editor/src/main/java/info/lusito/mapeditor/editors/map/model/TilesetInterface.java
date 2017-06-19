package info.lusito.mapeditor.editors.map.model;

import info.lusito.mapeditor.persistence.tileset.EcoTileset;

public interface TilesetInterface {

    EcoTileset getTileset();

    String getFilename();

    void focus();

    void selectTile(int x, int y);

    int getSelectedTileX();

    int getSelectedTileY();
    
    boolean isUsed();

    void remove();
}
