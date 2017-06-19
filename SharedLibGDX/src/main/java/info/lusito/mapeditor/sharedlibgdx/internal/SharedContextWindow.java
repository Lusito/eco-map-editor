package info.lusito.mapeditor.sharedlibgdx.internal;

import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JWindow;

/**
 * Creates a hidden JWindow for our shared opengl context.. kind of hacky, but I could not find a better way.
 */
public final class SharedContextWindow extends JWindow {

    public SharedContextWindow(LwjglAWTCanvas canvas) {
        final Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(canvas.getCanvas(), BorderLayout.CENTER);
        canvas.getGraphics().setContinuousRendering(false);

        pack();
        setOpacity(0);
        setVisible(true);
        setSize(100, 100);
        canvas.getCanvas().repaint();
        canvas.postRunnable(()->setVisible(false));
    }
}
