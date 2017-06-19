package info.lusito.mapeditor.editors.map.tools.select;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import info.lusito.mapeditor.sharedlibgdx.camera.SimpleCamera;
import info.lusito.mapeditor.persistence.shape.BoundingRect;
import info.lusito.mapeditor.persistence.shape.EcoCircle;
import info.lusito.mapeditor.sharedlibgdx.utils.InputUtil;
import info.lusito.mapeditor.sharedlibgdx.SharedContextProvider;
import org.openide.util.Lookup;

public class DragStateSelect extends DragState {

    private final BoundingRect selectionRect = new BoundingRect();
    private final Vector2 worldOrigin;

    private final Color lineColor = new Color(0, 0.6f, 1.0f, 1);
    private final Color bgColor = new Color(0, 0.6f, 1.0f, 0.2f);
    //fixme: dispose
    private final Texture lineX;
    private final Texture lineY;
    private final Texture background;

    public DragStateSelect(SimpleCamera camera, EcoCircle touchPoint, Vector2 worldOrigin) {
        super(camera, touchPoint);
        this.worldOrigin = worldOrigin;
        SharedContextProvider scp = Lookup.getDefault().lookup(SharedContextProvider.class);
        scp.pushContext();
        lineX = scp.getStippleTexture(8, 2, 4, 2);
        lineY = scp.getStippleTexture(2, 8, 2, 4);
        background = scp.getWhiteTexture();
        scp.popContext();
    }

    private void updateSelectionRect(int screenX, int screenY) {
        float zoom = camera.getZoom();
        float x = worldOrigin.x;
        float y = worldOrigin.y;
        float w = (screenX - originX) * zoom;
        float h = (screenY - originY) * zoom;
        if (w < 0) {
            w *= -1;
            x -= w;
        }
        if (h < 0) {
            h *= -1;
            y -= h;
        }
        selectionRect.set(x, y, w, h);
    }

    @Override
    public boolean start(int screenX, int screenY) {
        selectionRect.set(0, 0, 0, 0);
        return true;
    }

    @Override
    public void update(int screenX, int screenY) {
        updateSelectionRect(screenX, screenY);
    }

    @Override
    public void stop(int screenX, int screenY, boolean accept) {
        if (accept) {
            final boolean ctrlDown = InputUtil.isCtrlDown();
            final boolean shiftDown = InputUtil.isShiftDown();
            updateSelectionRect(screenX, screenY);

            if (ctrlDown && shiftDown) {
                listener.toggleSelection(selectionRect);
            } else if (ctrlDown) {
                listener.removeSelection(selectionRect);
            } else {
                if (!shiftDown) {
                    listener.clearSelection();
                }
                listener.addSelection(selectionRect);
            }
        }
    }

    @Override
    public void render(Batch batch, float zoomScale) {
        if (!(selectionRect.getWidth() == 0 && selectionRect.getHeight() == 0)) {
            batch.setColor(bgColor);
            float inset = 2 * zoomScale;
            float x = selectionRect.getX();
            float y = selectionRect.getY();
            float w = selectionRect.getWidth();
            float h = selectionRect.getHeight();
            batch.draw(background, x + inset, selectionRect.getY() + inset, w - inset, h - inset);
            batch.setColor(lineColor);
            drawLine(batch, lineY, x, y, 2, h / zoomScale, zoomScale);
            drawLine(batch, lineY, x + w, y, 2, h / zoomScale, zoomScale);
            drawLine(batch, lineX, x, y, w / zoomScale, 2, zoomScale);
            drawLine(batch, lineX, x, y + h, w / zoomScale, 2, zoomScale);
        }
    }

    private void drawLine(Batch batch, Texture texture, float x, float y, float width, float height, float zoomScale) {
        batch.draw(texture, x, y, width * zoomScale, height * zoomScale, 0, 0, (int) width,
                (int) height, false, true);
    }
}
