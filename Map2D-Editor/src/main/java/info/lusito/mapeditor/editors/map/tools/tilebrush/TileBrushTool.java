package info.lusito.mapeditor.editors.map.tools.tilebrush;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import info.lusito.mapeditor.editors.map.MapEditorController;
import info.lusito.mapeditor.sharedlibgdx.camera.SimpleCamera;
import info.lusito.mapeditor.editors.map.model.EcoMapFX;
import info.lusito.mapeditor.editors.map.model.EcoMapLayerFX;
import info.lusito.mapeditor.editors.map.tools.AbstractTool;
import info.lusito.mapeditor.editors.map.tools.select.TileSelectionManager;
import info.lusito.mapeditor.persistence.map.EcoMapLayer;
import info.lusito.mapeditor.persistence.map.EcoMapLayerType;
import info.lusito.mapeditor.persistence.map.EcoTileLayer;
import info.lusito.mapeditor.persistence.tileset.EcoTileInfo;
import javax.swing.JToggleButton;

public class TileBrushTool extends AbstractTool {

    protected final Vector2 dummyVectorA = new Vector2();
    protected final Vector2 dummyVectorB = new Vector2();
    protected final EcoTileLayer backupLayer;
    protected final DrawTilesInputHandler input = new DrawTilesInputHandler(this);
    protected final TileSelectionManager tileSelectionManager;

    public TileBrushTool(SimpleCamera simpleCamera, EcoMapFX mapFX, EcoTileLayer drawingLayer, TileSelectionManager tileSelectionManager) {
        this(simpleCamera, mapFX, drawingLayer, tileSelectionManager, createButton("pen", "Draw tiles", "Draw tiles"));
    }
    
    public TileBrushTool(SimpleCamera simpleCamera, EcoMapFX mapFX, EcoTileLayer drawingLayer, TileSelectionManager tileSelectionManager, JToggleButton button) {
        super(simpleCamera, mapFX, button);
        this.backupLayer = drawingLayer;
        this.tileSelectionManager = tileSelectionManager;
    }

    public InputProcessor getInput() {
        return input;
    }

    void startDrawTiles(int x, int y) {
        final EcoTileLayer focusTileLayer = getFocusTileLayer();
        if(focusTileLayer != null) {
            backupLayer.copyFrom(focusTileLayer);
            // convert to map coordinates
            dummyVectorA.set(x, y);
            camera.unproject(dummyVectorA);
            int aX = (int) (dummyVectorA.x / map.tileSize.x);
            int aY = (int) (dummyVectorA.y / map.tileSize.y);
            drawPixel(aX, aY);
        }
    }

    void drawTiles(int dragStartX, int dragStartY, int screenX, int screenY) {
        // convert to map coordinates
        dummyVectorA.set(dragStartX, dragStartY);
        camera.unproject(dummyVectorA);
        int aX = (int) (dummyVectorA.x / map.tileSize.x);
        int aY = (int) (dummyVectorA.y / map.tileSize.y);

        dummyVectorB.set(screenX, screenY);
        camera.unproject(dummyVectorB);
        // convert to tile coordinates
        int bX = (int) (dummyVectorB.x / map.tileSize.x);
        int bY = (int) (dummyVectorB.y / map.tileSize.y);
        // draw tiles
        drawLine(aX, aY, bX, bY);
    }

    void drawLine(int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int err = dx + dy, e2;

        while (true) {
            drawPixel(x0, y0);
            if (x0 == x1 && y0 == y1) {
                break;
            }
            e2 = 2 * err;
            if (e2 > dy) {
                err += dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    protected void drawPixel(int x, int y) {
        if (x < 0 || y < 0 || x >= map.size.x || y >= map.size.y) {
            return;
        }
        if (tileSelectionManager.hasSelection() && !tileSelectionManager.isSelected(x, y)) {
            return;
        }
        final EcoTileLayer layer = getFocusTileLayer();
        if (layer != null && mapFX.getPaintTile() != null) {
            layer.tiles[x][y] = mapFX.getPaintTile();
        }
    }

    protected EcoTileLayer getFocusTileLayer() {
        EcoMapLayerFX focusLayerFx = mapFX.getFocusLayerFX();
        if (focusLayerFx != null) {
            EcoMapLayer focusLayer = focusLayerFx.getLayer();
            if (focusLayer.getType() == EcoMapLayerType.TILE) {
                return (EcoTileLayer) focusLayer;
            }
        }
        return null;
    }

    protected void stopDrawTiles(boolean accept) {
        EcoTileLayer layer = getFocusTileLayer();
        if (accept) {
            EcoTileInfo[][] backup = UndoableTileLayerChange.createCopy(backupLayer.tiles);
            EcoTileInfo[][] copy = UndoableTileLayerChange.createCopy(layer.tiles);
            MapEditorController controller = mapFX.getController();
            controller.addUndoableEdit(new UndoableTileLayerChange(controller, layer, backup, copy));
        } else {
            if (layer != null) {
                layer.copyFrom(backupLayer);
            }
        }
        backupLayer.clearTiles();
    }
}
