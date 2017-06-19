package info.lusito.mapeditor.editors.map.tools.select;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import info.lusito.mapeditor.sharedlibgdx.camera.SimpleCamera;
import info.lusito.mapeditor.persistence.shape.EcoCircle;
import info.lusito.mapeditor.sharedlibgdx.utils.InputUtil;

public class SelectInputHandler extends InputAdapter {

    private DragState currentDragState;
    private final DragStateMove dragStateMove;
    private final DragStateSelect dragStateSelect;
    private final DragStateRotate dragStateRotate;
    private final DragStateScale dragStateScale;
    private final EcoCircle touchPoint = new EcoCircle();
    private final Vector2 worldOrigin = new Vector2();
    private boolean aborted;
    private int dragButton = -1;
    private final SelectTool tool;
    private SelectToolListener listener;
    private final SimpleCamera camera;

    public SelectInputHandler(SelectTool tool, SimpleCamera camera) {
        this.tool = tool;
        this.camera = camera;
        dragStateMove = new DragStateMove(camera, touchPoint);
        dragStateSelect = new DragStateSelect(camera, touchPoint, worldOrigin);
        dragStateRotate = new DragStateRotate(camera, touchPoint);
        dragStateScale = new DragStateScale(camera, touchPoint);
    }

    public void setSelectionListener(SelectToolListener listener) {
        this.listener = listener;
        dragStateMove.setSelectionListener(listener);
        dragStateSelect.setSelectionListener(listener);
        dragStateRotate.setSelectionListener(listener);
        dragStateScale.setSelectionListener(listener);
    }

    @Override
    public boolean keyDown(int keycode) {
        if (listener == null) {
            return false;
        }
        switch (keycode) {
            case Input.Keys.ESCAPE:
                aborted = true;
                if (currentDragState != null) {
                    stopDrag(0, 0, false);
                }
                return true;
            case Input.Keys.A:
                if (InputUtil.isCtrlDown()) {
                    if (InputUtil.isShiftDown()) {
                        listener.clearSelection();
                    } else {
                        listener.selectAll();
                    }
                    return true;
                }
                return false;
            case Input.Keys.I:
                if (InputUtil.isCtrlDown()) {
                    listener.invertSelection();
                    return true;
                }
                return false;
            case Input.Keys.FORWARD_DEL:
                listener.deleteSelection();
                return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (listener == null || dragButton == -1) {
            return false;
        }
        if (currentDragState == null) {
            if (dragButton == Input.Buttons.LEFT) {
                if (dragStateMove.start(screenX, screenY)) {
                    currentDragState = dragStateMove;
                } else if (dragStateSelect.start(screenX, screenY)) {
                    currentDragState = dragStateSelect;
                }
            } else if (dragButton == Input.Buttons.RIGHT) {
                if (dragStateRotate.start(screenX, screenY)) {
                    currentDragState = dragStateRotate;
                } else if (dragStateScale.start(screenX, screenY)) {
                    currentDragState = dragStateScale;
                }
            }
        }
        if (currentDragState != null) {
            currentDragState.update(screenX, screenY);
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (listener == null || dragButton != -1 || currentDragState != null) {
            return false;
        }

        if (button == Input.Buttons.LEFT || button == Input.Buttons.RIGHT) {
            dragButton = button;
            aborted = false;
            worldOrigin.set(screenX, screenY);
            camera.unproject(worldOrigin);
            touchPoint.center.set(worldOrigin.x, worldOrigin.y);
            touchPoint.radius = camera.getZoom() * 3;

            dragStateMove.setOrigin(screenX, screenY);
            dragStateSelect.setOrigin(screenX, screenY);
            dragStateRotate.setOrigin(screenX, screenY);
            dragStateScale.setOrigin(screenX, screenY);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (listener == null || dragButton != button) {
            return false;
        }
        if (aborted) {
            aborted = false;
        } else if (currentDragState != null) {
            stopDrag(screenX, screenY, true);
        } else {
            listener.touch(touchPoint);
        }
        dragButton = -1;
        return true;
    }

    private void stopDrag(int screenX, int screenY, boolean accept) {
        currentDragState.stop(screenX, screenY, accept);
        currentDragState = null;
        dragButton = -1;
    }

    public void render(Batch batch, float zoomScale) {
        if (currentDragState != null) {
            currentDragState.render(batch, zoomScale);
        }
    }
}
