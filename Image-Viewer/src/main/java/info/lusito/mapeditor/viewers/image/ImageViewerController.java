package info.lusito.mapeditor.viewers.image;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import info.lusito.mapeditor.projecttype.GameProject;
import info.lusito.mapeditor.sharedlibgdx.SharedContextProvider;
import info.lusito.mapeditor.sharedlibgdx.camera.SimpleCamera;
import info.lusito.mapeditor.sharedlibgdx.input.PanInputHandler;
import info.lusito.mapeditor.sharedlibgdx.input.ZoomInputHandler;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.fxml.Initializable;
import javax.swing.JToolBar;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;

public class ImageViewerController implements Initializable, ApplicationListener,
        PanInputHandler.Controller, ZoomInputHandler.Controller {

    private static final float ZOOM_UPPER_LIMIT = 256.0f;
    private static final float ZOOM_LOWER_LIMIT = 1 / 256.0f;
    private static final float ZOOM_INITIAL = 1;

    private final SimpleCamera simpleCamera = new SimpleCamera();
    private float camPosX, camPosY;
    public final InputMultiplexer inputMultiplexer = new InputMultiplexer();
    private Input input;
    private final Vector2 dummyVectorA = new Vector2();

    private final List<Runnable> readyCallbacks = new CopyOnWriteArrayList();
    private final Color backgroundColor = Color.valueOf("F0F0F0");
    private final JToolBar toolbar;
    private Texture texture;
    private SpriteBatch batch;
    private ImageDataObject dataObject;
    private int width;
    private int height;

    ImageViewerController(JToolBar toolbar) {
        this.toolbar = toolbar;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @Override
    public void create() {
        //fixme: this gets called on context recreation too, reset generated textures somehow!
        if (batch != null) {
            batch.dispose();
            batch = new SpriteBatch(5460);
            return;
        }
        batch = new SpriteBatch(5460);
        for (Runnable cb : readyCallbacks) {
            cb.run();
        }
        readyCallbacks.clear();
        input.setInputProcessor(inputMultiplexer);

        inputMultiplexer.addProcessor(new PanInputHandler(this));
        inputMultiplexer.addProcessor(new ZoomInputHandler(this));
        simpleCamera.setZoom(ZOOM_INITIAL);
        simpleCamera.update(0);

        final SharedContextProvider scp = Lookup.getDefault().lookup(SharedContextProvider.class);
        scp.addImageReloader((GameProject) getProject());
    }

    public Project getProject() {
        return FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        
        simpleCamera.resize(width, height);
        updateCameraPosition();
    }

    @Override
    public Input getInput() {
        return input;
    }

    @Override
    public void moveCamera(int diffX, int diffY) {
        camPosX += diffX;
        camPosY += diffY;
        updateCameraPosition();
    }


    @Override
    public void setZoom(float value) {
        if (value < ZOOM_LOWER_LIMIT) {
            value = ZOOM_LOWER_LIMIT;
        } else if (value > ZOOM_UPPER_LIMIT) {
            value = ZOOM_UPPER_LIMIT;
        }
        float zoom = getZoom();
        if (value != zoom) {
            zoom = value;

            Vector2 worldMouse = dummyVectorA.set(Gdx.input.getX(), Gdx.input.getY());
            simpleCamera.unproject(worldMouse);

            int dx = Gdx.input.getX() - Gdx.graphics.getWidth() / 2;
            int dy = Gdx.input.getY() - Gdx.graphics.getHeight() / 2;

            camPosX = Math.round(worldMouse.x / zoom - dx);
            camPosY = Math.round(worldMouse.y / zoom - dy);

            simpleCamera.setZoom(zoom);

            updateCameraPosition();
        }
    }

    @Override
    public float getZoom() {
        return simpleCamera.getZoom();
    }

    private void updateCameraPosition() {
        float x = width % 2 == 0 ? 0 : 0.5f;
        float y = height % 2 == 0 ? 0 : 0.5f;
        float zoom = simpleCamera.getZoom();
        simpleCamera.setCameraPosition(zoom * (camPosX + x), zoom * (camPosY + y));
        simpleCamera.update(0);
    }

    @Override
    public void render() {
        Gdx.gl20.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setColor(Color.WHITE);
        batch.begin();

        simpleCamera.bind(batch);
        int w = texture.getWidth();
        int h = texture.getHeight();
        batch.draw(texture, -w/2, -h/2, w, h, 0, 0, w, h, false, true);

        batch.flush();
        batch.end();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

    void setCanvas(LwjglAWTCanvas canvas) {
        input = canvas.getInput();
    }

    Color getBackgroundColor() {
        return backgroundColor;
    }

    public void onReady(Runnable runnable) {
        readyCallbacks.add(runnable);
    }

    void setDataObject(ImageDataObject dataObject) {
        this.dataObject = dataObject;
        SharedContextProvider scp = Lookup.getDefault().lookup(SharedContextProvider.class);
        //fixme: dispose texture
        texture = scp.getTexture(dataObject.getPrimaryEntry().getFile().getPath());
    }
}
