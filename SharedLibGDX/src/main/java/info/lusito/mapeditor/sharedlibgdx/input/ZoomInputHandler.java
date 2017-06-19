package info.lusito.mapeditor.sharedlibgdx.input;

import com.badlogic.gdx.InputAdapter;

public class ZoomInputHandler extends InputAdapter {

    private final Controller controller;

    public ZoomInputHandler(Controller controller) {
        this.controller = controller;
    }

    @Override
    public boolean scrolled(int amount) {
        float factor = amount > 0 ? 2 : 0.5f;
        controller.setZoom(controller.getZoom() * factor);
        return true;
    }

    public interface Controller {

        public float getZoom();

        public void setZoom(float zoom);
    }
}
