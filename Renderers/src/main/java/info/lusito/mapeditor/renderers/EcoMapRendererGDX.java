package info.lusito.mapeditor.renderers;


import info.lusito.mapeditor.model.EcoMapImageGDX;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import info.lusito.mapeditor.persistence.shape.BoundingRect;
import info.lusito.mapeditor.persistence.map.EcoImageLayer;
import info.lusito.mapeditor.persistence.map.EcoMap;
import info.lusito.mapeditor.persistence.map.EcoMapImage;
import info.lusito.mapeditor.persistence.map.EcoMapLayer;
import info.lusito.mapeditor.persistence.map.EcoMapLayerType;
import info.lusito.mapeditor.persistence.map.EcoMapTilesetReference;
import java.util.List;

/**
 * A map renderer which renders the Eco-Map with libgdx
 *
 * @author Santo Pfingsten
 */
public class EcoMapRendererGDX {

    final EcoMap map;
    final int mapTileWidth;
    final int mapTileHeight;
    final Color layerFilter = Color.WHITE.cpy();
    final ShapeRenderer shapeRenderer = new ShapeRenderer();
    final EcoMapLayerRendererGDX[] renderers = new EcoMapLayerRendererGDX[EcoMapLayerType.values().length];
    boolean drawLines;
    float stateTime = 0;

    public EcoMapRendererGDX(EcoMap map) {
        this.map = map;
        mapTileWidth = map.tileSize.x;
        mapTileHeight = map.tileSize.y;
    }

    public void setLayerRenderer(EcoMapLayerType type, EcoMapLayerRendererGDX renderer) {
        renderers[type.ordinal()] = renderer;
    }

    public EcoMap getMap() {
        return map;
    }

    public void setDrawLines(boolean drawLines) {
        this.drawLines = drawLines;
    }

    public void update(float delta) {
        stateTime += delta;
        for (EcoMapTilesetReference ref : map.tilesets) {
            ref.tileset.update(stateTime);
        }
        for (EcoMapLayer layer : map.layers) {
            if(layer.getType() == EcoMapLayerType.IMAGE) {
                List<EcoMapImage> images = ((EcoImageLayer)layer).images;
                for (EcoMapImage image : images) {
                    EcoMapImageGDX imageGDX = (EcoMapImageGDX) image.attachment;
                    imageGDX.update(delta);
                }
            }
        }
    }

    public void renderLayers(Batch batch, BoundingRect bounds) {
        for (EcoMapLayer layer : map.layers) {
            renderLayer(batch, layer, bounds);
        }
    }

    public void renderLayer(Batch batch, EcoMapLayer layer, BoundingRect bounds) {
        EcoMapLayerRendererGDX renderer = renderers[layer.getType().ordinal()];
        if (renderer != null && applyLayerOpacity(batch, layer)) {
            renderer.render(batch, layer, bounds);
            batch.setColor(Color.WHITE);
        }
    }

    private boolean applyLayerOpacity(Batch batch, EcoMapLayer layer) {
        if (layer.visible && layer.opacity > 0) {
            layerFilter.a = layer.opacity;
            batch.setColor(layerFilter);
            return true;
        }
        return false;
    }

    public void dispose() {
        for (EcoMapTilesetReference ref : map.tilesets) {
            Texture image = (Texture) ref.tileset.attachment;
            if (image != null) {
                ref.tileset.attachment = null;
                image.dispose();
            }
        }
    }
}
