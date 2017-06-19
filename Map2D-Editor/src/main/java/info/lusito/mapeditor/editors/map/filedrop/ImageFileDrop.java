package info.lusito.mapeditor.editors.map.filedrop;

import com.badlogic.gdx.graphics.Texture;
import info.lusito.mapeditor.editors.map.MapEditorController;
import info.lusito.mapeditor.editors.map.model.EcoMapFX;
import info.lusito.mapeditor.editors.map.model.EcoMapImageFX;
import info.lusito.mapeditor.editors.map.model.EcoMapLayerFX;
import info.lusito.mapeditor.editors.map.utils.FocusMode;
import info.lusito.mapeditor.editors.map.utils.UndoableMapEdit;
import info.lusito.mapeditor.persistence.animation.EcoAnimation;
import info.lusito.mapeditor.persistence.map.EcoImageLayer;
import info.lusito.mapeditor.persistence.map.EcoMapImage;
import info.lusito.mapeditor.persistence.map.EcoMapLayerType;
import info.lusito.mapeditor.projecttype.GameProjectUtil;
import info.lusito.mapeditor.utils.DialogUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import javax.swing.undo.CannotRedoException;
import org.openide.filesystems.FileObject;

public class ImageFileDrop implements FileDropListener {

    private final EcoMapFX mapFX;
    private final HashSet extensions = new HashSet();

    public ImageFileDrop(EcoMapFX mapFX) {
        this.mapFX = mapFX;
        extensions.add("png");
        extensions.add("jpg");
        extensions.add("jpeg");
        extensions.add("tga");
        //.xad handled manually, see onFileDrop
    }

    @Override
    public boolean onFileDrop(FileObject fileObject, String relativePath, float x, float y) {
        EcoMapLayerFX focusLayerFX = mapFX.getFocusLayerFX();
        if (focusLayerFX != null && focusLayerFX.getType() == EcoMapLayerType.IMAGE) {
            String ext = fileObject.getExt();
            if (!ext.isEmpty()) {
                ext = ext.toLowerCase();
                boolean isAnimation = ext.equals("xad");
                if (isAnimation || extensions.contains(ext)) {
                    EcoImageLayer layer = (EcoImageLayer) focusLayerFX.getLayer();
                    EcoMapImage image = new EcoMapImage();
                    image.x = x;
                    image.y = y;
                    image.filename = relativePath;
                    if(isAnimation) {
                        try(InputStream stream = GameProjectUtil.getInputStream(mapFX.getProjectDir(), relativePath)) {
                            EcoAnimation animation = EcoAnimation.load(stream);
                            Texture texture = mapFX.getTexture(animation.image.src);
                            image.attachment = new EcoMapImageFX(image, texture, animation);
                        } catch(IOException e) {
                            DialogUtil.showException("Error: Animation file could not be loaded: " + relativePath, e);
                            return false;
                        }
                    } else {
                        Texture texture = mapFX.getTexture(relativePath);
                        image.attachment = new EcoMapImageFX(image, texture);
                    }
                    layer.images.add(image);
                    addUndoableImageAdd(layer, image);
                    return true;
                }
            }
        }
        return false;
    }
    
    private void addUndoableImageAdd(EcoImageLayer layer, EcoMapImage image) {
        MapEditorController controller = mapFX.getController();
        controller.addUndoableEdit(new UndoableAdd(controller, layer, image));
    }

    private class UndoableAdd extends UndoableMapEdit {

        private final EcoImageLayer layer;
        private final EcoMapImage image;

        public UndoableAdd(MapEditorController controller, EcoImageLayer layer, EcoMapImage image) {
            super(controller);
            this.layer = layer;
            this.image = image;
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            applyLayerFocus(FocusMode.NONE);
            layer.images.remove(image);
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            applyLayerFocus(FocusMode.NONE);
            layer.images.add(image);
        }
    }
}
