package info.lusito.mapeditor.editors.map.filedrop;

import com.badlogic.gdx.graphics.Texture;
import info.lusito.mapeditor.editors.map.MapEditorController;
import info.lusito.mapeditor.editors.map.model.EcoMapEntityFX;
import info.lusito.mapeditor.editors.map.model.EcoMapFX;
import info.lusito.mapeditor.editors.map.model.EcoMapLayerFX;
import info.lusito.mapeditor.editors.map.utils.FocusMode;
import info.lusito.mapeditor.editors.map.utils.UndoableMapEdit;
import info.lusito.mapeditor.persistence.entity.EcoEntity;
import info.lusito.mapeditor.persistence.map.EcoEntityLayer;
import info.lusito.mapeditor.persistence.map.EcoMapEntity;
import info.lusito.mapeditor.persistence.map.EcoMapLayerType;
import info.lusito.mapeditor.service.filewatcher.WatchedFile;
import javax.swing.undo.CannotRedoException;
import org.openide.filesystems.FileObject;

public class EntityFileDrop implements FileDropListener {

    private final EcoMapFX mapFX;

    public EntityFileDrop(EcoMapFX mapFX) {
        this.mapFX = mapFX;
    }

    @Override
    public boolean onFileDrop(FileObject fileObject, String relativePath, float x, float y) {
        EcoMapLayerFX focusLayerFX = mapFX.getFocusLayerFX();
        if (focusLayerFX != null && focusLayerFX.getType() == EcoMapLayerType.ENTITY) {
            String ext = fileObject.getExt();
            if (ext.equalsIgnoreCase("xed")) {
                EcoEntityLayer layer = (EcoEntityLayer) focusLayerFX.getLayer();
                WatchedFile file = mapFX.getProject().getFileWatcher("xed").getFile(relativePath);
                EcoEntity ed = (EcoEntity) file.getContent();
                Texture texture = mapFX.getTexture(ed.image);
                EcoMapEntity entity = new EcoMapEntity();
                entity.x = x;
                entity.y = y;
                entity.type = relativePath;
                entity.attachment = new EcoMapEntityFX(entity, ed, texture);
                layer.entities.add(entity);
                addUndoableEntityAdd(layer, entity);
                return true;
            }
        }
        return false;
    }
    
    private void addUndoableEntityAdd(EcoEntityLayer layer, EcoMapEntity entity) {
        MapEditorController controller = mapFX.getController();
        controller.setSelectTool();
        controller.addUndoableEdit(new UndoableAdd(controller, layer, entity));
    }

    private class UndoableAdd extends UndoableMapEdit {

        private final EcoEntityLayer layer;
        private final EcoMapEntity entity;

        public UndoableAdd(MapEditorController controller, EcoEntityLayer layer, EcoMapEntity entity) {
            super(controller);
            this.layer = layer;
            this.entity = entity;
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            applyLayerFocus(FocusMode.NONE);
            layer.entities.remove(entity);
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            applyLayerFocus(FocusMode.NONE);
            layer.entities.add(entity);
        }
    }
}
