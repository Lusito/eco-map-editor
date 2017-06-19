package info.lusito.mapeditor.editors.map.tools.select;

import com.badlogic.gdx.graphics.g2d.Batch;
import info.lusito.mapeditor.sharedlibgdx.camera.SimpleCamera;
import info.lusito.mapeditor.persistence.shape.EcoCircle;

public abstract class DragState {

    protected int originX, originY;
    protected SelectToolListener listener;
    protected final SimpleCamera camera;
    protected final EcoCircle touchPoint;

    DragState(SimpleCamera camera, EcoCircle touchPoint) {
        this.camera = camera;
        this.touchPoint = touchPoint;
    }

    public void setSelectionListener(SelectToolListener listener) {
        this.listener = listener;
    }

    public void setOrigin(int screenX, int screenY) {
        originX = screenX;
        originY = screenY;
    }

    public abstract boolean start(int screenX, int screenY);

    public abstract void stop(int screenX, int screenY, boolean accept);

    public abstract void update(int screenX, int screenY);

    public void render(Batch batch, float zoomScale) {
    }
}
