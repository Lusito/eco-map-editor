package info.lusito.mapeditor.renderers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import info.lusito.mapeditor.persistence.shape.BoundingRect;
import info.lusito.mapeditor.persistence.map.EcoMap;
import info.lusito.mapeditor.persistence.map.EcoMapLayer;
import info.lusito.mapeditor.persistence.map.EcoMapTilesetReference;
import info.lusito.mapeditor.persistence.map.EcoTileLayer;
import info.lusito.mapeditor.persistence.tileset.EcoTileInfo;
import info.lusito.mapeditor.persistence.tileset.EcoTileset;

public class EcoTileLayerRendererGDX implements EcoMapLayerRendererGDX {

    final EcoMap map;

    public EcoTileLayerRendererGDX(EcoMap map) {
        this.map = map;
    }

    @Override
    public void render(Batch batch, EcoMapLayer layer, BoundingRect bounds) {
        render(batch, (EcoTileLayer) layer, bounds);
    }

    private void render(Batch batch, EcoTileLayer layer, BoundingRect bounds) {
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
        for (int tileY = startY; tileY <= endY; tileY++) {
            renderLine(batch, layer, startX, startY, endX, tileY);
        }
    }

    /**
     * Render a section of this tile layer
     * 
     * @param startX
     *            The x tile location to start rendering
     * @param startY
     *            The y tile location to start rendering
     * @param endX
     *            The last x tile to render
     * @param tileY
     *            The line of tiles to render
     */
    private void renderLine(Batch batch, EcoTileLayer layer, int startX, int startY,
            int endX, int tileY) {
        EcoTileInfo[][] tiles = layer.tiles;
        for (EcoMapTilesetReference ref : map.tilesets) {
            final EcoTileset tileset = ref.tileset;

            Texture image = null;
            for (int tileX = startX; tileX <= endX; tileX++) {
                EcoTileInfo info = tiles[tileX][tileY];
                if (info != null && info.tileset == tileset) {
                    if (image == null) {
                        image = (Texture) tileset.attachment;
                    }

                    float px = tileX * map.tileSize.x;
                    float py = tileY * map.tileSize.y;

                    batch.draw(image, px, py,
                            tileset.grid.x, tileset.grid.y,
                            info.textureX, info.textureY,
                            tileset.grid.x, tileset.grid.y,
                            false, true);
                }
            }
        }
    }
}
