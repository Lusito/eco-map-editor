package info.lusito.mapeditor.sharedlibgdx.camera;

/**
 * A fixed camera that is centered on the screen.
 * This can be used to draw UI and HUD for example.
 *
 * @author Santo Pfingsten
 */
public class ScreenCamera extends SimpleCamera {

    protected void onViewportChanged(float width, float height) {
        setCameraPosition(width / 2, height / 2);
        camera.update(true);
    }
}
