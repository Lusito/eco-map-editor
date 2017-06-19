package info.lusito.mapeditor.utils;

import java.awt.EventQueue;
import javafx.application.Platform;
import javax.swing.SwingUtilities;

public class InvokeUtil {

    public static void runOnEDT(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            EventQueue.invokeLater(runnable);
        }
    }

    public static void runOnFX(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }
}
