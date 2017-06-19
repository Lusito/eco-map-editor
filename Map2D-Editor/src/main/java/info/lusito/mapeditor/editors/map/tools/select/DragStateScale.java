package info.lusito.mapeditor.editors.map.tools.select;

import com.badlogic.gdx.math.Vector2;
import info.lusito.mapeditor.sharedlibgdx.camera.SimpleCamera;
import info.lusito.mapeditor.persistence.shape.EcoCircle;
import info.lusito.mapeditor.sharedlibgdx.utils.InputUtil;

public class DragStateScale extends DragState {

    private final Vector2 scalePoint = new Vector2();

    public DragStateScale(SimpleCamera camera, EcoCircle touchPoint) {
        super(camera, touchPoint);
    }

    @Override
    public boolean start(int screenX, int screenY) {
        if (!InputUtil.isShiftDown()) {
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
        scalePoint.set(screenX, screenY);
        camera.unproject(scalePoint);
        listener.scaleStart(scalePoint.x, scalePoint.y);
        return true;
    }

    @Override
    public void update(int screenX, int screenY) {
        scalePoint.set(screenX, screenY);
        camera.unproject(scalePoint);
        listener.scaleUpdate(scalePoint.x, scalePoint.y);
    }

    @Override
    public void stop(int screenX, int screenY, boolean accept) {
        if (accept) {
            listener.scaleFinish();
        } else {
            listener.scaleCancel();
        }
    }
}
