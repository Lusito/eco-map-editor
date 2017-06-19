package info.lusito.mapeditor.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import info.lusito.mapeditor.persistence.entity.EcoEntity;
import info.lusito.mapeditor.persistence.map.EcoMapEntity;
import info.lusito.mapeditor.persistence.shape.EcoRectangle;

public class EcoMapEntityGDX {

    public Texture texture;
    public final EcoRectangle rect = new EcoRectangle();
    public final EcoEntity definition;
    public final Color shapeColor;

    public EcoMapEntityGDX(EcoMapEntity entity, EcoEntity definition, Texture texture) {
        this.texture = texture;
        this.definition = definition;
        shapeColor = definition.shapeColor == null ? Color.CYAN : Color.valueOf(definition.shapeColor);
        if (texture != null) {
            updateRect(entity);
        }
    }
    
    public EcoMapEntityGDX(EcoMapEntity entity, EcoMapEntityGDX other) {
        texture = other.texture;
        definition = other.definition;
        shapeColor = other.shapeColor;
        if (texture != null) {
            updateRect(entity);
        }
    }

    public final void updateRect(EcoMapEntity entity) {
        final float width = texture.getWidth() * entity.scale;
        final float height = texture.getHeight() * entity.scale;
        rect.set(entity.x, entity.y, width, height, entity.rotation);
    }
}
