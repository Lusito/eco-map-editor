package info.lusito.mapeditor.viewers.image;

import info.lusito.mapeditor.common.AbstractViewer;
import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import info.lusito.mapeditor.sharedlibgdx.SharedContextProvider;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Image;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.openide.util.*;

public final class ImageViewer extends AbstractViewer<ImageDataObject> {

    private static final long serialVersionUID = 3649239182134506884L;

    private ImageViewerController controller;

    public ImageViewer() {
        super();
    }

    public ImageViewer(ImageDataObject obj) {
        initialize(obj);
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
            controller.setDataObject(dataObject);
        });
        this.revalidate();
    }

    @Override
    protected void setupPanel() {
        controller = new ImageViewerController(toolbar);
        SharedContextProvider scp = Lookup.getDefault().lookup(SharedContextProvider.class);
        scp.init(this::onReady);
    }

    @Override
    protected void componentClosed() {
        //fixme: free texture
        super.componentClosed();
    }

    @Override
    protected JToolBar createToolBar() {
        JToolBar toolBar = super.createToolBar();
        toolBar.setName("image viewer toolbar");
        JToggleButton button = new JToggleButton("hello");
        toolBar.add(button);
        toolBar.addSeparator();
        return toolBar;
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage("info/lusito/mapeditor/viewers/image/icon.png");
    }
}
