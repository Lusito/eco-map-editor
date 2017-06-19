package info.lusito.mapeditor.model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import info.lusito.mapeditor.persistence.animation.EcoAnimation;
import info.lusito.mapeditor.persistence.animation.EcoAnimationHelper;
import info.lusito.mapeditor.utils.EcoLoaderGDX;

import java.util.Random;

public class EcoMapAnimationGDX extends EcoAnimationHelper {

    private final TextureRegion textureRegion = new TextureRegion();
    private int width;
    private int height;
    private int columns;
    private float stateTime;
    private int lastIndex = -1;
    private final EcoLoaderGDX ecoLoader;

    public EcoMapAnimationGDX(Random random, Texture texture, EcoLoaderGDX ecoLoader) {
        super(random);
        textureRegion.setTexture(texture);
        this.ecoLoader = ecoLoader;
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void update(float delta) {
        stateTime += delta;
        final int index = getFrameIndex(stateTime);
        if (index != lastIndex) {
            int row = index / columns;
            int col = index % columns;
            textureRegion.setRegion(col * width, row * height, width, height);
            textureRegion.flip(false, true);
            lastIndex = index;
        }
    }
    
    public void setStateTime(float stateTime) {
        this.stateTime = stateTime;
    }
    
    public boolean isDone() {
        switch (mode) {
            case NORMAL:
            case REVERSED:
                return stateTime >= totalDuration;
        }
        return false;
    }

    public void play(EcoAnimation animation) {
        lastIndex = -1;
        if(ecoLoader != null) {
            textureRegion.setTexture(ecoLoader.getTexture(animation.image.src));
        }
        width = animation.image.width / animation.grid.x;
        height = animation.image.height / animation.grid.y;
        columns = animation.grid.x;
        int idxLimit = animation.grid.x * animation.grid.y;
        setup(animation.clip.durations, animation.clip.frames, idxLimit, animation.clip.mode);
    }
}
