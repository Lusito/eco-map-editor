package info.lusito.mapeditor.editors.map.model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import info.lusito.mapeditor.model.EcoMapImageGDX;
import info.lusito.mapeditor.persistence.animation.EcoAnimation;
import info.lusito.mapeditor.persistence.map.EcoMapImage;

public class EcoMapImageFX extends EcoMapImageGDX {

    public final State backup = new State();

    public EcoMapImageFX(EcoMapImage image, Texture texture) {
        super(image, texture);
    }

    public EcoMapImageFX(EcoMapImage image, Texture texture, EcoAnimation animation) {
        super(image, texture, animation);
    }

    public EcoMapImageFX(EcoMapImage image, EcoMapImageFX other) {
        super(image, other);
    }

    public State createState() {
        return new State();
    }

    public class State {

        public final Vector2 pos = new Vector2();
        public float rotation;
        public float scale;

        public void readFrom(State other) {
            pos.set(other.pos);
            rotation = other.rotation;
            scale = other.scale;
        }
        
        public void readFrom(EcoMapImage image) {
            pos.set(image.x, image.y);
            scale = image.scale;
            rotation = image.rotation;
        }
        
        public void writeTo(EcoMapImage image) {
            image.x = pos.x;
            image.y = pos.y;
            image.scale = scale;
            image.rotation = rotation;
            updateRect(image);
        }
    }
}
