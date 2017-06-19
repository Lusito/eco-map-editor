package info.lusito.mapeditor.editors.map.model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import info.lusito.mapeditor.model.EcoMapEntityGDX;
import info.lusito.mapeditor.editors.properties.api.PropertiesGroupInterface;
import info.lusito.mapeditor.persistence.entity.EcoEntity;
import info.lusito.mapeditor.persistence.map.EcoMapEntity;
import info.lusito.mapeditor.persistence.shape.EcoShape;
import java.util.List;

public class EcoMapEntityFX extends EcoMapEntityGDX {

    public final State backup = new State();
    public List<PropertiesGroupInterface> groups;

    public EcoMapEntityFX(EcoMapEntity entity, EcoEntity definition, Texture texture) {
        super(entity, definition, texture);
    }

    public EcoMapEntityFX(EcoMapEntity entity, EcoMapEntityFX other) {
        super(entity, other);
    }
    
    public State createState() {
        return new State();
    }

    public class State {

        public final Vector2 pos = new Vector2();
        public float rotation;
        public float scale;
        public EcoShape shape;

        public void readFrom(State other) {
            pos.set(other.pos);
            rotation = other.rotation;
            scale = other.scale;
            shape = other.shape == null ? null : other.shape.copy();
        }

        public void readFrom(EcoMapEntity entity) {
            pos.set(entity.x, entity.y);
            scale = entity.scale;
            rotation = entity.rotation;
            shape = entity.shape == null ? null : entity.shape.copy();
        }

        public void writeTo(EcoMapEntity entity) {
            entity.x = pos.x;
            entity.y = pos.y;
            entity.scale = scale;
            entity.rotation = rotation;
            entity.shape = shape == null ? null : shape.copy();
            updateRect(entity);
        }
    }
}
