package info.lusito.mapeditor.editors.map.tools.tileeraser;

import info.lusito.mapeditor.sharedlibgdx.camera.SimpleCamera;
import info.lusito.mapeditor.editors.map.model.EcoMapFX;
import info.lusito.mapeditor.editors.map.tools.select.TileSelectionManager;
import info.lusito.mapeditor.editors.map.tools.tilebrush.TileBrushTool;
import info.lusito.mapeditor.persistence.map.EcoTileLayer;

public class TileEraserTool extends TileBrushTool {

    public TileEraserTool(SimpleCamera simpleCamera, EcoMapFX mapFX, EcoTileLayer drawingLayer, TileSelectionManager tileSelectionManager) {
        super(simpleCamera, mapFX, drawingLayer, tileSelectionManager, createButton("eraser", "Erase tiles", "Erase tiles"));
    }
    
    @Override
    protected void drawPixel(int x, int y) {
        if (x < 0 || y < 0 || x >= map.size.x || y >= map.size.y) {
            return;
        }
        if (tileSelectionManager.hasSelection() && !tileSelectionManager.isSelected(x, y)) {
            return;
        }
        final EcoTileLayer layer = getFocusTileLayer();

        if (layer != null) {
            layer.tiles[x][y] = null;
        }
    }
}
