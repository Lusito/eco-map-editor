package info.lusito.mapeditor.renderers;

import info.lusito.mapeditor.model.EcoMapImageGDX;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import info.lusito.mapeditor.persistence.shape.BoundingRect;
import info.lusito.mapeditor.persistence.map.EcoMap;
import info.lusito.mapeditor.persistence.map.EcoMapLayer;
import info.lusito.mapeditor.persistence.map.EcoImageLayer;
import info.lusito.mapeditor.persistence.map.EcoMapImage;

public class EcoImageLayerRendererGDX implements EcoMapLayerRendererGDX {

    final EcoMap map;

    public EcoImageLayerRendererGDX(EcoMap map) {
        this.map = map;
    }

    @Override
    public void render(Batch batch, EcoMapLayer layer, BoundingRect bounds) {
        render(batch, (EcoImageLayer) layer, bounds);
    }

    private void render(Batch batch, EcoImageLayer layer, BoundingRect bounds) {
        for (EcoMapImage image : layer.images) {
            if (image.attachment == null) {
                continue;
            }
            if (isVisible(image, bounds)) {
                EcoMapImageGDX imageGDX = (EcoMapImageGDX) image.attachment;
                TextureRegion region = imageGDX.getTextureRegion();
                final float width = region.getRegionWidth() * image.scale;
                float halfWidth = width * 0.5f;
                final float height = region.getRegionHeight() * image.scale;
                float halfHeight = height * 0.5f;
                batch.draw(region, image.x - halfWidth, image.y - halfHeight, halfWidth, halfHeight,
                        width, height, 1, 1, image.rotation);
            }
        }
    }

    private boolean isVisible(EcoMapImage image, BoundingRect bounds) {
        EcoMapImageGDX imageGDX = (EcoMapImageGDX) image.attachment;
        return bounds.intersects(imageGDX.rect);
    }
}
