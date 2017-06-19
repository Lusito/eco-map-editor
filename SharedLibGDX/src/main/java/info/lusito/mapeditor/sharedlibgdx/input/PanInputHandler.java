package info.lusito.mapeditor.sharedlibgdx.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

public class PanInputHandler extends InputAdapter {

    private int dragStartX, dragStartY;
    private boolean dragging;
    private final Controller controller;

    public PanInputHandler(Controller controller) {
        this.controller = controller;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if (dragging) {
            doDrag(screenX, screenY);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (dragging) {
            doDrag(screenX, screenY);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.MIDDLE) {
            initDrag(screenX, screenY);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.MIDDLE) {
            stopDrag();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.SPACE) {
            Input input = controller.getInput();
            initDrag(input.getX(), input.getY());
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.SPACE) {
            stopDrag();
            return true;
        }
        return false;
    }

    private void doDrag(int screenX, int screenY) {
        int diffX = screenX - dragStartX;
        int diffY = screenY - dragStartY;
        dragStartX = screenX;
        dragStartY = screenY;
        controller.moveCamera(-diffX, -diffY);
    }

    private void initDrag(int screenX, int screenY) {
        if (!dragging) {
            dragStartX = screenX;
            dragStartY = screenY;
            dragging = true;
        }
    }

    private void stopDrag() {
        dragging = false;
    }

    public interface Controller {

        public Input getInput();

        public void moveCamera(int diffX, int diffY);
    }

}
