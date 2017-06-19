package info.lusito.mapeditor.editors.map.tools.select;

import com.badlogic.gdx.math.Vector2;
import info.lusito.mapeditor.sharedlibgdx.camera.SimpleCamera;
import info.lusito.mapeditor.persistence.shape.EcoCircle;
import info.lusito.mapeditor.sharedlibgdx.utils.InputUtil;

public class DragStateRotate extends DragState {

    private final Vector2 rotatePoint = new Vector2();

    public DragStateRotate(SimpleCamera camera, EcoCircle touchPoint) {
        super(camera, touchPoint);
    }

    @Override
    public boolean start(int screenX, int screenY) {
        if (InputUtil.isShiftDown()) {
            return false;
        } else if (InputUtil.isAltDown()) {
            if (!listener.hasSelection() && !listener.setMoveSelection(touchPoint)) {
                return false;
            }
        } else {
            if (!listener.testSelection(touchPoint) && !listener.setMoveSelection(touchPoint)) {
                return false;
            }
        }
        rotatePoint.set(screenX, screenY);
        camera.unproject(rotatePoint);
        listener.rotateStart(rotatePoint.x, rotatePoint.y);
        return true;
    }

    @Override
    public void update(int screenX, int screenY) {
        rotatePoint.set(screenX, screenY);
        camera.unproject(rotatePoint);
        listener.rotateUpdate(rotatePoint.x, rotatePoint.y);
    }

    @Override
    public void stop(int screenX, int screenY, boolean accept) {
        if (accept) {
            listener.rotateFinish();
        } else {
            listener.rotateCancel();
        }
    }
}
