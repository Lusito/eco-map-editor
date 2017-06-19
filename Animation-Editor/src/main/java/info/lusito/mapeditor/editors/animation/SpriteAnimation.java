package info.lusito.mapeditor.editors.animation;

import info.lusito.mapeditor.persistence.animation.EcoAnimation;
import info.lusito.mapeditor.persistence.animation.EcoAnimationHelper;
import java.util.Random;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class SpriteAnimation extends Transition {

    public static Random random = new Random();

    private final ImageView imageView;
    private int width;
    private int height;
    private int columns;

    private int lastIndex = -1;
    private final EcoAnimationHelper helper = new EcoAnimationHelper(random);

    public SpriteAnimation(ImageView imageView) {
        this.imageView = imageView;
        setInterpolator(Interpolator.LINEAR);
    }

    public void play(EcoAnimation animation) {
        lastIndex = -1;
        width = animation.image.width / animation.grid.x;
        height = animation.image.height / animation.grid.y;
        columns = animation.grid.x;
        int indexLimit = animation.grid.x*animation.grid.y;
        helper.setup(animation.clip.durations, animation.clip.frames, indexLimit, animation.clip.mode);

        setCycleDuration(Duration.seconds(helper.totalDuration));
        switch (animation.clip.mode) {
            default:
            case NORMAL:
            case REVERSED:
                setCycleCount(1);
                break;
            case LOOP:
            case LOOP_REVERSED:
            case LOOP_PINGPONG:
            case LOOP_RANDOM:
                setCycleCount(Animation.INDEFINITE);
                break;
        }
        playFromStart();
    }

    protected void interpolate(double k) {
        double t = k * helper.totalDuration;
        final int index = helper.getFrameIndex(t);
        if (index != lastIndex) {
            int row = index / columns;
            int col = index % columns;
            imageView.setViewport(new Rectangle2D(col * width, row * height, width, height));
            lastIndex = index;
        }
    }
}
