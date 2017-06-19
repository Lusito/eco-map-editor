package info.lusito.mapeditor.editors.animation;

import info.lusito.mapeditor.common.AbstractEditorFX;
import java.awt.Image;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import org.openide.util.ImageUtilities;

public final class AnimationEditor
        extends AbstractEditorFX<AnimationDataObject, AnimationEditorController> {

    private static final long serialVersionUID = -2216328729285967344L;

    public AnimationEditor() {
    }

    public AnimationEditor(AnimationDataObject obj) {
        initialize(obj);
    }

    @Override
    protected void initController() {
        controller.setDataObject(dataObject, manager);
    }

    @Override
    protected FXMLLoader loadFXML() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AnimationEditor.fxml"));
        fxmlLoader.load();
        return fxmlLoader;
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage("info/lusito/mapeditor/editors/animation/icon.png");
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        if(controller != null) {
            controller.onClose();
        }
    }
}
