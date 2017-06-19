package info.lusito.mapeditor.editors.map.tools.tilebucket;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import info.lusito.mapeditor.editors.map.MapEditorController;
import info.lusito.mapeditor.sharedlibgdx.camera.SimpleCamera;
import info.lusito.mapeditor.editors.map.model.EcoMapFX;
import info.lusito.mapeditor.editors.map.model.EcoMapLayerFX;
import info.lusito.mapeditor.editors.map.tools.AbstractTool;
import info.lusito.mapeditor.editors.map.tools.select.TileSelectionManager;
import info.lusito.mapeditor.editors.map.tools.tilebrush.UndoableTileLayerChange;
import info.lusito.mapeditor.persistence.map.EcoMapLayer;
import info.lusito.mapeditor.persistence.map.EcoMapLayerType;
import info.lusito.mapeditor.persistence.map.EcoTileLayer;
import info.lusito.mapeditor.persistence.tileset.EcoTileInfo;
import java.awt.Point;
import java.util.Stack;
import javax.swing.JToggleButton;

public class TileBucketTool extends AbstractTool {

    private final Vector2 dummyVectorA = new Vector2();
    private final FillTilesInputHandler input = new FillTilesInputHandler(this);
    private final TileSelectionManager tileSelectionManager;
    // fixme: recycle points?
    private final Stack<Point> fillStack = new Stack();
    private final JToggleButton globalModeButton;

    public TileBucketTool(SimpleCamera simpleCamera, EcoMapFX mapFX, TileSelectionManager tileSelectionManager) {
        super(simpleCamera, mapFX, createButton("fill", "Fill tiles", "Fill tiles"));
        this.tileSelectionManager = tileSelectionManager;
        this.globalModeButton = createButton("fillglobal", "Global fill mode", "Global Mode");
    }

    public JToggleButton getGlobalModeButton() {
        return globalModeButton;
    }

    @Override
    public void setButtonEnabled(boolean enabled) {
        super.setButtonEnabled(enabled);
        globalModeButton.setEnabled(enabled && isSelected());
    }

    @Override
    public void select() {
        super.select();
        globalModeButton.setEnabled(true);
    }

    @Override
    public void deselect() {
        super.deselect();
        globalModeButton.setEnabled(false);
    }

    @Override
    public InputProcessor getInput() {
        return input;
    }

    void fillTiles(int x, int y) {
        // convert to map coordinates
        dummyVectorA.set(x, y);
        camera.unproject(dummyVectorA);
        int aX = (int) (dummyVectorA.x / map.tileSize.x);
        int aY = (int) (dummyVectorA.y / map.tileSize.y);
        drawPixel(aX, aY);
    }

    void drawPixel(int x, int y) {
        if (x < 0 || y < 0 || x >= map.size.x || y >= map.size.y) {
            return;
        }
        final EcoTileInfo paintTile = mapFX.getPaintTile();
        EcoTileLayer layer = getFocusTileLayer();
        if (paintTile != null && layer != null) {
            EcoTileInfo oldTile = layer.tiles[x][y];
            if(oldTile != paintTile) {
                
                EcoTileInfo[][] backup = UndoableTileLayerChange.createCopy(layer.tiles);
                if(globalModeButton.isSelected()) {
                    fillGlobally(layer, oldTile, paintTile);
                } else {
                    fill4(layer, new Point(x, y), oldTile, paintTile);
                }
                EcoTileInfo[][] copy = UndoableTileLayerChange.createCopy(layer.tiles);
                MapEditorController controller = mapFX.getController();
                controller.addUndoableEdit(new UndoableTileLayerChange(controller, layer, backup, copy));
            }
        }
    }

    private void fillGlobally(EcoTileLayer layer,EcoTileInfo oldTile, EcoTileInfo newTile) {
        boolean hasSelection = tileSelectionManager.hasSelection();
        boolean[][] selected = tileSelectionManager.getTiles();
        for (int y = 0; y < map.size.y; y++) {
            for (int x = 0; x < map.size.x; x++) {
                if((!hasSelection || selected[x][y])
                        && layer.tiles[x][y] == oldTile) {
                    layer.tiles[x][y] = newTile;
                }
            }
        }
    }

    private void fill4(EcoTileLayer layer, Point p, EcoTileInfo oldTile, EcoTileInfo newTile) {

        fillStack.push(p);

        boolean hasSelection = tileSelectionManager.hasSelection();
        while (!fillStack.isEmpty()) {
            p = fillStack.pop();

            if(hasSelection && !tileSelectionManager.isSelected(p.x, p.y))
                continue;
            if (layer.tiles[p.x][p.y] == oldTile) {
                layer.tiles[p.x][p.y] = newTile;

                if (p.y < (map.size.y - 1)) {
                    fillStack.push(new Point(p.x, p.y + 1));
                }
                if (p.y > 0) {
                    fillStack.push(new Point(p.x, p.y - 1));
                }
                if (p.x < (map.size.x - 1)) {
                    fillStack.push(new Point(p.x + 1, p.y));
                }
                if (p.x > 0) {
                    fillStack.push(new Point(p.x - 1, p.y));
                }
            }
        }
    }

    EcoTileLayer getFocusTileLayer() {
        EcoMapLayerFX focusLayerFx = mapFX.getFocusLayerFX();
        if (focusLayerFx != null) {
            EcoMapLayer focusLayer = focusLayerFx.getLayer();
            if (focusLayer.getType() == EcoMapLayerType.TILE) {
                return (EcoTileLayer) focusLayer;
            }
        }
        return null;
    }
}
