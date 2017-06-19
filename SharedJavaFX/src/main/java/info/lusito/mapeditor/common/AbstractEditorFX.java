package info.lusito.mapeditor.common;

import info.lusito.mapeditor.utils.DialogUtil;
import java.awt.BorderLayout;
import java.io.IOException;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import org.netbeans.api.actions.Savable;
import org.openide.loaders.MultiDataObject;
import org.openide.util.Exceptions;

public abstract class AbstractEditorFX<DOT extends MultiDataObject, CT extends Savable>
        extends AbstractEditor<DOT, CT> {

    private static final long serialVersionUID = -6084120184639865073L;

    protected final JFXPanel panel = new JFXPanel();

    @Override
    protected void setupPanel() {
        add(panel, BorderLayout.CENTER);
        Platform.setImplicitExit(false);
        Platform.runLater(this::createScene);
    }

    protected abstract void initController();
    
    
    protected abstract FXMLLoader loadFXML() throws IOException;

    private void createScene() {
        try {
            FXMLLoader fxmlLoader = loadFXML();
            controller = fxmlLoader.<CT>getController();
            initController();

            Scene scene = new Scene(fxmlLoader.getRoot());
            panel.setScene(scene);
            scene.getStylesheets().add("info/lusito/mapeditor/common/style.css");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            DialogUtil.showException("Error creating scene", ex);
        }
    }
}
