package info.lusito.mapeditor.editors.map.tools.select;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import info.lusito.mapeditor.editors.map.MapEditorController;
import info.lusito.mapeditor.persistence.shape.BoundingRect;
import info.lusito.mapeditor.persistence.shape.EcoCircle;
import info.lusito.mapeditor.editors.map.tools.tilebrush.UndoableTileLayerChange;
import info.lusito.mapeditor.editors.map.utils.FocusMode;
import info.lusito.mapeditor.editors.map.utils.UndoableMapEdit;
import info.lusito.mapeditor.persistence.map.EcoMap;
import info.lusito.mapeditor.persistence.map.EcoTileLayer;
import info.lusito.mapeditor.persistence.tileset.EcoTileInfo;
import info.lusito.mapeditor.sharedlibgdx.SharedContextProvider;
import java.util.Arrays;
import javax.swing.undo.CannotRedoException;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;

public class TileSelectionManager implements SelectToolListener {

    private boolean[][] tiles;
    private int numSelectedTiles;
    private EcoMap map;
    private int fromX;
    private int fromY;
    private int toX;
    private int toY;

    private final Color color = new Color(0, 0.6f, 1.0f, 0.6f);
    private final Texture texture;
    private EcoTileLayer layer;
    private final MapEditorController controller;

    public TileSelectionManager(MapEditorController controller) {
        this.controller = controller;
        texture = Lookup.getDefault().lookup(SharedContextProvider.class).getWhiteTexture();
    }

    public void setMap(EcoMap map) {
        tiles = new boolean[map.size.x][map.size.y];
        numSelectedTiles = 0;
        this.map = map;
    }
    
    public void setFocusLayer(EcoTileLayer layer) {
        this.layer = layer;
    }

    public boolean[][] getTiles() {
        return tiles;
    }

    private void calculateFromTo(BoundingRect rect) {
        fromX = (int) (rect.getX() / map.tileSize.x);
        fromY = (int) (rect.getY() / map.tileSize.y);
        toX = (int) (rect.getRight() / map.tileSize.x);
        toY = (int) (rect.getBottom() / map.tileSize.y);
        if (fromX < 0) {
            fromX = 0;
        }
        if (fromY < 0) {
            fromY = 0;
        }
        if (toX >= map.size.x) {
            toX = map.size.x - 1;
        }
        if (toY >= map.size.y) {
            toY = map.size.y - 1;
        }
    }
    
    @Override
    public boolean hasSelection() {
        return numSelectedTiles > 0;
    }
    
    private boolean[][] copyTiles() {
        boolean[][] copy = new boolean[map.size.x][map.size.y];
        for (int i = 0; i < tiles.length; i++) {
            System.arraycopy(tiles[i], 0, copy[i], 0, tiles[i].length);
        }
        return copy;
    }

    private void addUndoableSelectionChange(boolean[][] from, int fromCount, boolean[][] to, int toCount) {
        updateSelectionStatus();
        controller.addUndoableEdit(new UndoableSelectionChange(controller, from, fromCount, to, toCount));
    }
    
    private void updateSelectionStatus() {
        if(numSelectedTiles > 0) {
            int minX = map.size.x-1;
            int minY = map.size.y-1;
            int maxX = 0;
            int maxY = 0;
            for (int tx = 0; tx < map.size.x; tx++) {
                for (int ty = 0; ty < map.size.y; ty++) {
                    if(tiles[tx][ty]) {
                        if(tx < minX)
                            minX = tx;
                        if(tx > maxX)
                            maxX = tx;
                        if(ty < minY)
                            minY = ty;
                        if(ty > maxY)
                            maxY = ty;
                    }
                }
            }
            int width = (maxX - minX) + 1;
            int height = (maxY - minY) + 1;
            StatusDisplayer.getDefault().setStatusText(
                    "Selection (" + numSelectedTiles + " tiles): " + width + "x"
                    + height + " (at " + minX + "," + minY + ")");
        } else {
            StatusDisplayer.getDefault().setStatusText("No selection");
        }
    }

    @Override
    public void clearSelection() {
        if(numSelectedTiles != 0) {
            boolean[][] copy = tiles;
            tiles = new boolean[map.size.x][map.size.y];
            int lastCount = numSelectedTiles;
            numSelectedTiles = 0;
            addUndoableSelectionChange(copy, lastCount, null, numSelectedTiles);
        }
    }
    
    @Override
    public boolean testSelection(info.lusito.mapeditor.persistence.shape.EcoCircle circle) {
//        int tx = (int) (x / map.tileSize.x);
//        int ty = (int) (y / map.tileSize.y);
//        if (tx >= 0 && ty >= 0 && tx < map.size.x && ty < map.size.y) {
//            return tiles[tx][ty];
//        }
        return false;
    }
    
    @Override
    public void touch(EcoCircle circle) {
        clearSelection();
    }
    
    @Override
    public boolean setMoveSelection(EcoCircle circle) {
        return false;
    }

    @Override
    public void addSelection(BoundingRect rect) {
        calculateFromTo(rect);
        boolean[][] backup = null;
        boolean changed = false;
        int lastCount = numSelectedTiles;
        for (int tx = fromX; tx <= toX; tx++) {
            for (int ty = fromY; ty <= toY; ty++) {
                if(!tiles[tx][ty]) {
                    if(!changed) {
                        backup = numSelectedTiles == 0 ? null : copyTiles();
                        changed = true;
                    }
                    tiles[tx][ty] = true;
                    numSelectedTiles++;
                }
            }
        }
        if(changed) {
            boolean[][] copy = numSelectedTiles == 0 ? null : copyTiles();
            addUndoableSelectionChange(backup, lastCount, copy, numSelectedTiles);
        }
    }

    @Override
    public void removeSelection(BoundingRect rect) {
        calculateFromTo(rect);
        boolean[][] backup = null;
        boolean changed = false;
        int lastCount = numSelectedTiles;
        for (int tx = fromX; tx <= toX; tx++) {
            for (int ty = fromY; ty <= toY; ty++) {
                if(tiles[tx][ty]) {
                    if(!changed) {
                        backup = numSelectedTiles == 0 ? null : copyTiles();
                        changed = true;
                    }
                    tiles[tx][ty] = false;
                    numSelectedTiles--;
                }
            }
        }
        if(changed) {
            boolean[][] copy = numSelectedTiles == 0 ? null : copyTiles();
            addUndoableSelectionChange(backup, lastCount, copy, numSelectedTiles);
        }
    }

    @Override
    public void toggleSelection(BoundingRect rect) {
        calculateFromTo(rect);
        boolean[][] backup = null;
        boolean changed = false;
        int lastCount = numSelectedTiles;
        for (int tx = fromX; tx <= toX; tx++) {
            for (int ty = fromY; ty <= toY; ty++) {
                if(!changed) {
                    backup = numSelectedTiles == 0 ? null : copyTiles();
                    changed = true;
                }
                if(tiles[tx][ty]) {
                    tiles[tx][ty] = false;
                    numSelectedTiles--;
                } else {
                    tiles[tx][ty] = true;
                    numSelectedTiles++;
                }
            }
        }
        if(changed) {
            boolean[][] copy = numSelectedTiles == 0 ? null : copyTiles();
            addUndoableSelectionChange(backup, lastCount, copy, numSelectedTiles);
        }
    }
    
    @Override
    public void invertSelection() {
        boolean[][] backup = numSelectedTiles == 0 ? null : copyTiles();
        int lastCount = numSelectedTiles;
        for (int tx = 0; tx < map.size.x; tx++) {
            for (int ty = 0; ty < map.size.y; ty++) {
                tiles[tx][ty] = !tiles[tx][ty];
            }
        }
        numSelectedTiles = (map.size.x * map.size.y) - numSelectedTiles;
        boolean[][] copy = numSelectedTiles == 0 ? null : copyTiles();
        addUndoableSelectionChange(backup, lastCount, copy, numSelectedTiles);
    }
    
    @Override
    public void selectAll(){
        int numTiles = map.size.x * map.size.y;
        if(numSelectedTiles != numTiles) {
            boolean[][] backup = numSelectedTiles == 0 ? null : copyTiles();
            int lastCount = numSelectedTiles;
            for (boolean[] row : tiles) {
                Arrays.fill(row, true);
            }
            numSelectedTiles = numTiles;
            boolean[][] copy = numSelectedTiles == 0 ? null : copyTiles();
            addUndoableSelectionChange(backup, lastCount, copy, numSelectedTiles);
        }
    }
    
    @Override
    public void deleteSelection() {
        if(layer != null && layer.tiles != null && hasSelection()) {
            EcoTileInfo[][] copy = UndoableTileLayerChange.createCopy(layer.tiles);
            for (int tx = 0; tx < map.size.x; tx++) {
                for (int ty = 0; ty < map.size.y; ty++) {
                    if(tiles[tx][ty]) {
                        layer.tiles[tx][ty] = null;
                    }
                }
            }
            controller.addUndoableEdit(new UndoableTileLayerChange(controller, layer, copy, null));
        }
    }
    
    @Override
    public void moveStart() {
        //fixme: store positions for all selected entities/images
    }
    
    @Override
    public void moveUpdate(float x, float y) {
        
    }

    @Override
    public void moveFinish(){
        //fixme: create undo entry/finalize move
    }

    @Override
    public void moveCancel(){
        //fixme: undo move
    }

    @Override
    public void rotateStart(float x, float y) {
    }

    @Override
    public void rotateUpdate(float x, float y) {
    }

    @Override
    public void rotateFinish() {
    }

    @Override
    public void rotateCancel() {
    }

    @Override
    public void scaleStart(float x, float y) {
    }

    @Override
    public void scaleUpdate(float x, float y) {
    }

    @Override
    public void scaleFinish() {
    }

    @Override
    public void scaleCancel() {
    }

    public void render(Batch batch, BoundingRect bounds) {
        if(layer == null || !hasSelection())
            return;
        
        int startX = (int) Math.floor(bounds.getX() / (float) map.tileSize.x);
        int startY = (int) Math.floor(bounds.getY() / (float) map.tileSize.y);
        int tilesX = (int) Math.ceil(bounds.getWidth() / (float) map.tileSize.x) + 1;
        int tilesY = (int) Math.ceil(bounds.getHeight() / (float) map.tileSize.y) + 1;

        int endX = Math.min(map.size.x-1, startX + tilesX);
        if(startX < 0)
            startX = 0;
        int endY = Math.min(map.size.y-1, startY + tilesY);
        if(startY < 0)
            startY = 0;
        batch.setColor(color);
        for (int tx = startX; tx <= endX; tx++) {
            for (int ty = startY; ty <= endY; ty++) {
                if(tiles[tx][ty]) {
                    drawTile(batch, tx * map.tileSize.x, ty * map.tileSize.y,  map.tileSize.x, map.tileSize.y);
                }
            }
        }
    }
    
    private void drawTile(Batch batch, float x, float y, float width, float height) {
        batch.draw(texture, x, y, width, height);
    }

    public boolean isSelected(int tx, int ty) {
        if (tx >= 0 && ty >= 0 && tx < map.size.x && ty < map.size.y) {
            return tiles[tx][ty];
        }
        return false;
    }

    private class UndoableSelectionChange extends UndoableMapEdit {

        private final boolean[][] from;
        private final int fromCount;
        private final boolean[][] to;
        private final int toCount;

        public UndoableSelectionChange(MapEditorController controller, boolean[][] from, int fromCount, boolean[][] to, int toCount) {
            super(controller);
            this.from = from;
            this.fromCount = fromCount;
            this.to = to;
            this.toCount = toCount;
        }

        private void performUndoRedo(boolean[][] copy, int count) {
            applyLayerFocus(FocusMode.LAYER);
            if(count == 0) {
                for (boolean[] row : tiles) {
                    Arrays.fill(row, false);
                }
            } else {
                for (int i = 0; i < copy.length; i++) {
                    System.arraycopy(copy[i], 0, tiles[i], 0, tiles[i].length);
                }
            }
            numSelectedTiles = count;
            updateSelectionStatus();
        }
        
        @Override
        protected void performUndo() throws CannotRedoException {
            performUndoRedo(from, fromCount);
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            performUndoRedo(to, toCount);
        }
    }

}
