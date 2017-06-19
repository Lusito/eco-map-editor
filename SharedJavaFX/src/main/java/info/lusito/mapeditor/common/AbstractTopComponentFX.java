package info.lusito.mapeditor.common;

import info.lusito.mapeditor.utils.DialogUtil;
import java.awt.BorderLayout;
import java.io.IOException;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

public abstract class AbstractTopComponentFX<T> extends TopComponent {

    protected T controller;
    protected final JFXPanel panel;

    public AbstractTopComponentFX() {
        setLayout(new BorderLayout());
        putClientProperty(TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);

        panel = new JFXPanel();
        add(panel, BorderLayout.CENTER);
        Platform.setImplicitExit(false);
        Platform.runLater(this::createScene);
    }

    protected void createScene() {
        try {

            FXMLLoader fxmlLoader = loadFXML();
            Parent root = fxmlLoader.getRoot();
            controller = fxmlLoader.<T>getController();

            Scene scene = new Scene(root);
            panel.setScene(scene);
            scene.getStylesheets().add("info/lusito/mapeditor/common/style.css");
            onSceneCreated();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            DialogUtil.showException("Error creating scene", ex);
        }
    }
    
    protected abstract FXMLLoader loadFXML() throws IOException;

    protected void onSceneCreated() {
    }
}
