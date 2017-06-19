package info.lusito.mapeditor.renderers;

import com.badlogic.gdx.graphics.g2d.Batch;
import info.lusito.mapeditor.persistence.shape.BoundingRect;
import info.lusito.mapeditor.persistence.map.EcoMapLayer;

public interface EcoMapLayerRendererGDX {

    void render(Batch batch, EcoMapLayer layer, BoundingRect bounds);
}
