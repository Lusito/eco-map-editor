package info.lusito.mapeditor.sharedlibgdx.camera;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Base class for orthographic camera controllers.
 *
 * @author Santo Pfingsten
 */
public class SimpleCamera {

    private final Vector3 dummy = new Vector3();

    protected final OrthographicCamera camera = new OrthographicCamera() {
        public void setToOrtho(boolean yDown, float viewportWidth, float viewportHeight) {
            if (yDown) {
                up.set(0, -1, 0);
                direction.set(0, 0, 1);
            } else {
                up.set(0, 1, 0);
                direction.set(0, 0, -1);
            }
            this.viewportWidth = viewportWidth;
            this.viewportHeight = viewportHeight;
            onViewportChanged(viewportWidth, viewportHeight);
            update(true);
        }
    };

    public final void resize(int width, int height) {
        camera.setToOrtho(true, width, height);
    }

    protected void onViewportChanged(float width, float height) {

    }

    public void update(float delta) {
        camera.update(true);
    }

    public void setCameraPosition(float x, float y) {
        camera.position.x = x;
        camera.position.y = y;
    }

    public void setCameraPosition(Vector3 pos) {
        camera.position.x = pos.x;
        camera.position.y = pos.y;
        camera.position.z = pos.z;
    }

    public void setZoom(float newZoom) {
        camera.zoom = newZoom;
    }

    public float getZoom() {
        return camera.zoom;
    }

    public float getLeftOffset() {
        return camera.position.x - camera.viewportWidth / 2;
    }

    public float getTopOffset() {
        return camera.position.y - camera.viewportHeight / 2;
    }

    public final void bind(Batch batch) {
        batch.setProjectionMatrix(camera.combined);
    }

    /**
     * Convert point from screen to world coordinates.
     * 
     * @param point the point in screen coordinates
     * @see OrthographicCamera#unproject
     */
    public void unproject(Vector2 point) {
        dummy.set(point.x, point.y, 0);
        camera.unproject(dummy);
        point.x = dummy.x;
        point.y = dummy.y;
    }

    /**
     * Convert point from world to screen coordinates.
     * 
     * @param point the point in world coordinates
     * @see OrthographicCamera#project
     */
    public void project(Vector2 point) {
        dummy.set(point.x, point.y, 0);
        camera.project(dummy);
        point.x = dummy.x;
        point.y = dummy.y;
    }
}
