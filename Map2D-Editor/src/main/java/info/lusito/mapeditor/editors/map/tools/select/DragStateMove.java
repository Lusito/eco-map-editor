package info.lusito.mapeditor.editors.map.tools.select;

import info.lusito.mapeditor.sharedlibgdx.camera.SimpleCamera;
import info.lusito.mapeditor.persistence.shape.EcoCircle;
import info.lusito.mapeditor.sharedlibgdx.utils.InputUtil;

public class DragStateMove extends DragState {

    public DragStateMove(SimpleCamera camera, EcoCircle touchPoint) {
        super(camera, touchPoint);
    }

    @Override
    public boolean start(int screenX, int screenY) {
        if (InputUtil.isAltDown()) {
            if (!listener.hasSelection() && !listener.setMoveSelection(touchPoint)) {
                return false;
            }
        } else if (!InputUtil.isShiftDown() && !InputUtil.isCtrlDown()) {
            if (!listener.testSelection(touchPoint) && !listener.setMoveSelection(touchPoint)) {
                return false;
            }
        } else {
            return false;
        }
        listener.moveStart();
        return true;
    }

    @Override
    public void update(int screenX, int screenY) {
        float zoom = camera.getZoom();
        listener.moveUpdate((screenX - originX) * zoom, (screenY - originY) * zoom);
    }

    @Override
    public void stop(int screenX, int screenY, boolean accept) {
        if (accept) {
            listener.moveFinish();
        } else {
            listener.moveCancel();
        }
    }
}
