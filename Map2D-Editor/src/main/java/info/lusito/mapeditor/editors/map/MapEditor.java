package info.lusito.mapeditor.editors.map;

import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import info.lusito.mapeditor.common.AbstractEditor;
import info.lusito.mapeditor.editors.properties.api.adapters.PropertiesAdapter;
import info.lusito.mapeditor.projecttype.GameProject;
import info.lusito.mapeditor.sharedlibgdx.SharedContextProvider;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Image;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

public final class MapEditor extends AbstractEditor<MapDataObject, MapEditorController> {

    private static final long serialVersionUID = -4879883390649946588L;

    protected transient MultiViewElementCallback callback;
    private PropertiesAdapter properties;

    public MapEditor() {
    }

    public MapEditor(MapDataObject obj) {
        initialize(obj);
    }

    @Override
    protected void setupPanel() {
        properties = new PropertiesAdapter((GameProject) FileOwnerQuery.getOwner(dataObject.getPrimaryFile()));
        controller = new MapEditorController(toolbar);
        SharedContextProvider scp = Lookup.getDefault().lookup(SharedContextProvider.class);
        scp.init(this::onReady);
    }

    private void onReady() {
        SharedContextProvider scp = Lookup.getDefault().lookup(SharedContextProvider.class);
        LwjglAWTCanvas sharedContext = scp.getCanvas();
        LwjglApplicationConfiguration.disableAudio = true;
        LwjglAWTCanvas awtCanvas = new LwjglAWTCanvas(controller, sharedContext);
        controller.setCanvas(awtCanvas);
        final Canvas canvas = awtCanvas.getCanvas();
        Color bg = controller.getBackgroundColor();
        canvas.setBackground(new java.awt.Color(bg.r, bg.g, bg.b, bg.a));
        this.add(canvas, BorderLayout.CENTER);
        controller.onReady(() -> {
            controller.setDataObject(dataObject, manager);
            controller.setPropertiesEditor(properties);
        });
        this.revalidate();
    }

    @Override
    protected void componentClosed() {
        properties.close();
        controller.onClose();
        super.componentClosed();
    }

    @Override
    public void componentShowing() {
        controller.onShowing();
        content.add(properties);
        content.add(controller.getMap());
        super.componentShowing();
    }

    @Override
    public void componentHidden() {
        controller.onHidden();
        content.remove(properties);
        content.remove(controller.getMap());
        super.componentHidden();
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage("info/lusito/mapeditor/editors/map/icon.png");
    }
}
