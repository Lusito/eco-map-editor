package info.lusito.mapeditor.editors.map.tools.terrainbrush;

import com.badlogic.gdx.math.RandomXS128;
import info.lusito.mapeditor.sharedlibgdx.camera.SimpleCamera;
import info.lusito.mapeditor.editors.map.model.EcoMapFX;
import info.lusito.mapeditor.editors.map.tools.select.TileSelectionManager;
import info.lusito.mapeditor.editors.map.tools.tilebrush.TileBrushTool;
import info.lusito.mapeditor.persistence.map.EcoTileLayer;
import info.lusito.mapeditor.persistence.tileset.EcoTileInfo;
import java.util.List;

public class TerrainBrushTool extends TileBrushTool {
    
    RandomXS128 random = new RandomXS128();

    public TerrainBrushTool(SimpleCamera simpleCamera, EcoMapFX mapFX, EcoTileLayer drawingLayer, TileSelectionManager tileSelectionManager) {
        super(simpleCamera, mapFX, drawingLayer, tileSelectionManager, createButton("terrain", "Draw terrain", "Draw terrain"));
    }
    
    @Override
    protected void drawPixel(int x, int y) {
        if (x < 0 || y < 0 || x >= map.size.x || y >= map.size.y) {
            return;
        }
        final EcoTileLayer layer = getFocusTileLayer();
        EcoTileInfo paintTile = mapFX.getPaintTile();
        if(layer == null || paintTile == null || paintTile.terrainBits == 0) {
            return;
        }
        int a = (paintTile.terrainBits & 0xFF000000) >> 24;
        int b = (paintTile.terrainBits & 0x00FF0000) >> 16;
        int c = (paintTile.terrainBits & 0x0000FF00) >> 8;
        int d = paintTile.terrainBits & 0x000000FF;
        if(a != b || b != c || c != d) {
            return;
        }
        if (tileSelectionManager.hasSelection() && !tileSelectionManager.isSelected(x, y)) {
            return;
        }
        EcoTileInfo tile = layer.tiles[x][y];
        if(tile == null || tile.terrainBits != paintTile.terrainBits) {
            List<EcoTileInfo> tiles = mapFX.getTerrainInfo()
                    .terrainMap.get(paintTile.terrainBits);
            if(tiles != null && !tiles.isEmpty()) {
                layer.tiles[x][y] = tiles.get(random.nextInt(tiles.size()));
            }
        }
        paintNeighbour(paintTile, layer, x-1, y-1, 0xFFFFFF00);
        paintNeighbour(paintTile, layer, x, y-1, 0xFFFF0000);
        paintNeighbour(paintTile, layer, x+1, y-1, 0xFFFF00FF);
        paintNeighbour(paintTile, layer, x-1, y, 0xFF00FF00);
        paintNeighbour(paintTile, layer, x+1, y, 0x00FF00FF);
        paintNeighbour(paintTile, layer, x-1, y+1, 0xFF00FFFF);
        paintNeighbour(paintTile, layer, x, y+1, 0x0000FFFF);
        paintNeighbour(paintTile, layer, x+1, y+1, 0x00FFFFFF);
    }

    private void paintNeighbour(EcoTileInfo paintTile, EcoTileLayer layer,
            int x, int y, int keepBitmask) {
        if (x < 0 || y < 0 || x >= map.size.x || y >= map.size.y) {
            return;
        }
        EcoTileInfo tile = layer.tiles[x][y];
        if (tile == null || tile.terrainBits == 0
                || (tileSelectionManager.hasSelection()
                && !tileSelectionManager.isSelected(x, y))) {
            return;
        }
        int neededTerrainBits = ( (tile.terrainBits & keepBitmask)
                | (paintTile.terrainBits & ~keepBitmask));
        if(tile.terrainBits != neededTerrainBits) {
            List<EcoTileInfo> tiles = mapFX.getTerrainInfo()
                    .terrainMap.get(neededTerrainBits);
            if(tiles != null && !tiles.isEmpty()) {
                layer.tiles[x][y] = tiles.get(random.nextInt(tiles.size()));
            }
        }
    }
}
