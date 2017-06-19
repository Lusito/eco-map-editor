package info.lusito.mapeditor.model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.RandomXS128;
import info.lusito.mapeditor.persistence.animation.EcoAnimation;
import info.lusito.mapeditor.persistence.map.EcoMapImage;
import info.lusito.mapeditor.persistence.shape.EcoRectangle;
import java.util.Random;

public class EcoMapImageGDX {

    public static Random random = new RandomXS128();

    public final EcoRectangle rect = new EcoRectangle();
    private final String relativePath;
    private Texture texture;
    private TextureRegion textureRegion;
    private EcoMapAnimationGDX mapAnimation;
    private EcoAnimation animation;

    public EcoMapImageGDX(EcoMapImage image, Texture texture) {
        this.relativePath = image.filename;
        this.texture = texture;
        textureRegion = new TextureRegion(texture);
        textureRegion.flip(false, true);
        updateRect(image);
    }

    public EcoMapImageGDX(EcoMapImage image, Texture texture, EcoAnimation animation) {
        this.relativePath = image.filename;
        this.texture = texture;
        this.animation = animation;
        mapAnimation = new EcoMapAnimationGDX(random, texture, null);
        mapAnimation.play(animation);
        updateRect(image);
    }

    public EcoMapImageGDX(EcoMapImage image, EcoMapImageGDX other) {
        relativePath = other.relativePath;
        texture = other.texture;
        animation = other.animation;
        if(other.mapAnimation != null) {
            mapAnimation = new EcoMapAnimationGDX(random, texture, null);
            mapAnimation.play(animation);
        } else {
            textureRegion = new TextureRegion(texture);
            textureRegion.flip(false, true);
        }
        updateRect(image);
    }

    public final void updateRect(EcoMapImage image) {
        final float width = getWidth() * image.scale;
        final float height = getHeight() * image.scale;
        rect.set(image.x, image.y, width, height, image.rotation);
    }
    
    public final int getWidth() {
        return mapAnimation == null ? texture.getWidth() : mapAnimation.getWidth();
    }

    public final int getHeight() {
        return mapAnimation == null ? texture.getHeight() : mapAnimation.getHeight();
    }

    public final TextureRegion getTextureRegion() {
        return mapAnimation == null ? textureRegion : mapAnimation.getTextureRegion();
    }

    public void update(float delta) {
        if (mapAnimation != null) {
            mapAnimation.update(delta);
        }
    }

    public String getRelativePath() {
        return relativePath;
    }
}
