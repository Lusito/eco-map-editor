package info.lusito.mapeditor.editors.map.tools.select;

import info.lusito.mapeditor.editors.map.tools.select.properties.ImagePropertiesGroup;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import info.lusito.mapeditor.editors.map.MapEditorController;
import info.lusito.mapeditor.editors.map.model.EcoMapImageFX;
import info.lusito.mapeditor.editors.map.model.EcoMapLayerFX;
import info.lusito.mapeditor.editors.map.utils.FocusMode;
import info.lusito.mapeditor.editors.map.utils.UndoableMapEdit;
import info.lusito.mapeditor.persistence.shape.BoundingRect;
import info.lusito.mapeditor.persistence.shape.EcoCircle;
import info.lusito.mapeditor.persistence.shape.EcoPoint;
import info.lusito.mapeditor.editors.properties.api.adapters.PropertiesAdapter;
import info.lusito.mapeditor.persistence.map.EcoImageLayer;
import info.lusito.mapeditor.persistence.map.EcoMapImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.undo.CannotRedoException;

public class ImageSelectionManager implements SelectToolListener {

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private EcoMapLayerFX currentLayerFX;
    private EcoImageLayer currentLayer;
    private List<EcoMapImage> selection;
    private final Vector2 selectionCenter = new Vector2();
    private boolean drawSelectionCenter;
    private final Vector2 dummyVector = new Vector2();
    private float rotateStartAngle;
    private float scaleStartLength;
    private final MapEditorController controller;
    private final ImagePropertiesGroup imageProperties;

    public ImageSelectionManager(MapEditorController controller) {
        this.controller = controller;
        imageProperties = new ImagePropertiesGroup(controller);
    }

    public void setFocusLayer(EcoMapLayerFX layerFX, EcoImageLayer currentLayer, boolean refocusSelection) {
        this.currentLayerFX = layerFX;
        this.currentLayer = currentLayer;
        selection = layerFX == null ? null : layerFX.selection;
        if(selection != null && refocusSelection) {
            onSelectionChange();
        }
    }

    private void addUndoableSelectionChange(List<EcoMapImage> from, List<EcoMapImage> to) {
        controller.addUndoableEdit(new UndoableSelectionChange(controller, from, to));
        onSelectionChange();
    }

    @Override
    public boolean hasSelection() {
        return selection != null && !selection.isEmpty();
    }

    @Override
    public void clearSelection() {
        if (selection != null && !selection.isEmpty()) {
            List<EcoMapImage> backup = selection;
            setSelection(new ArrayList());
            addUndoableSelectionChange(backup, selection);
        }
    }

    private void setSelection(List<EcoMapImage> list) {
        selection = list;
        currentLayerFX.selection = list;
    }

    private List<EcoMapImage> duplicateSelection() {
        List<EcoMapImage> old = selection;
        setSelection(new ArrayList(selection));
        return old;
    }

    private void onSelectionChange() {
        PropertiesAdapter editor = controller.getPropertiesEditor();
        editor.groups.clear();
        editor.loaded = true;
        editor.title = "Image";
        editor.instance = false;
        if (selection != null && selection.size() == 1) {
            imageProperties.setImage(selection.get(0));
            editor.groups.add(imageProperties);
        }
        editor.updateEverything();
    }

    @Override
    public boolean testSelection(EcoCircle circle) {
        if (selection != null) {
            for (EcoMapImage image : selection) {
                if (((EcoMapImageFX) image.attachment).rect.intersects(circle)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean setMoveSelection(EcoCircle circle) {
        EcoMapImage image = find(circle);
        if (image == null) {
            clearSelection();
            return false;
        }

        List<EcoMapImage> backup = selection;
        setSelection(new ArrayList());
        selection.add(image);
        addUndoableSelectionChange(backup, selection);
        return true;
    }

    @Override
    public void touch(EcoCircle circle) {
        EcoMapImage image = find(circle);
        if (hasSelection()) {
            if (image == null) {
                clearSelection();
            } else if (!selection.contains(image)) {
                List<EcoMapImage> backup = selection;
                setSelection(new ArrayList());
                selection.add(image);
                addUndoableSelectionChange(backup, selection);
            }
        } else if (image != null) {
            List<EcoMapImage> backup = duplicateSelection();
            selection.add(image);
            addUndoableSelectionChange(backup, selection);
        }
    }

    public EcoMapImage find(EcoCircle circle) {
        if (currentLayer != null) {
            for (int i = currentLayer.images.size() - 1; i >= 0; i--) {
                EcoMapImage image = currentLayer.images.get(i);
                if (((EcoMapImageFX) image.attachment).rect.intersects(circle)) {
                    return image;
                }
            }
        }
        return null;
    }

    @Override
    public void addSelection(BoundingRect rect) {
        if (currentLayer != null) {
            List<EcoMapImage> backup = null;
            for (EcoMapImage image : currentLayer.images) {
                if (!selection.contains(image) && intersects(image, rect)) {
                    if (backup == null) {
                        backup = duplicateSelection();
                    }
                    selection.add(image);
                }
            }
            if (backup != null) {
                addUndoableSelectionChange(backup, selection);
            }
        }
    }

    @Override
    public void removeSelection(BoundingRect rect) {
        if (currentLayer != null) {
            List<EcoMapImage> backup = null;
            for (EcoMapImage image : currentLayer.images) {
                if (selection.contains(image) && intersects(image, rect)) {
                    if (backup == null) {
                        backup = duplicateSelection();
                    }
                    selection.remove(image);
                }
            }
            if (backup != null) {
                addUndoableSelectionChange(backup, selection);
            }
        }
    }

    @Override
    public void toggleSelection(BoundingRect rect) {
        if (currentLayer != null) {
            List<EcoMapImage> backup = null;
            for (EcoMapImage image : currentLayer.images) {
                if (intersects(image, rect)) {
                    if (backup == null) {
                        backup = duplicateSelection();
                    }
                    if (selection.contains(image)) {
                        selection.remove(image);
                    } else {
                        selection.add(image);
                    }
                }
            }
            if (backup != null) {
                addUndoableSelectionChange(backup, selection);
            }
        }
    }

    private void updateSelectionCenter() {
        selectionCenter.setZero();
        float count = 0;
        for (EcoMapImage image : selection) {
            selectionCenter.x += image.x;
            selectionCenter.y += image.y;
            count++;
        }
        if (count > 0) {
            selectionCenter.scl(1 / count);
        }
    }

    private void backupSelectionState() {
        for (EcoMapImage image : selection) {
            EcoMapImageFX imageFX = (EcoMapImageFX) image.attachment;
            imageFX.backup.readFrom(image);
        }
    }

    private void restoreSelectionState() {
        for (EcoMapImage image : selection) {
            EcoMapImageFX imageFX = (EcoMapImageFX) image.attachment;
            imageFX.backup.writeTo(image);
        }
    }

    private void addUndoableStateChange() {
        UndoableStateChange entry = new UndoableStateChange(controller);
        for (EcoMapImage image : selection) {
            entry.changes.add(new ImageStateChange(image));
        }
        controller.addUndoableEdit(entry);
        controller.getPropertiesEditor().updateValues();
    }

    private static boolean intersects(EcoMapImage image, BoundingRect rect) {
        EcoMapImageFX imageFX = (EcoMapImageFX) image.attachment;
        return rect.intersects(imageFX.rect);
    }

    @Override
    public void invertSelection() {
        if (currentLayer.images.isEmpty()) {
            selection.clear(); // should be clear, but just in case.
        } else {
            List<EcoMapImage> newSelection = new ArrayList();
            for (EcoMapImage image : currentLayer.images) {
                if (!selection.contains(image)) {
                    newSelection.add(image);
                }
            }
            List<EcoMapImage> backup = selection;
            setSelection(newSelection);
            addUndoableSelectionChange(backup, selection);
        }
    }

    @Override
    public void selectAll() {
        List<EcoMapImage> backup = selection;
        setSelection(new ArrayList(currentLayer.images));
        addUndoableSelectionChange(backup, selection);
    }

    @Override
    public void deleteSelection() {
        controller.addUndoableEdit(new UndoableDelete(controller, currentLayer, selection));
        for (EcoMapImage image : selection) {
            currentLayer.images.remove(image);
        }
        setSelection(new ArrayList());
    }

    @Override
    public void moveStart() {
        backupSelectionState();
    }

    @Override
    public void moveUpdate(float x, float y) {
        for (EcoMapImage image : selection) {
            EcoMapImageFX imageFX = (EcoMapImageFX) image.attachment;
            image.x = imageFX.backup.pos.x + x;
            image.y = imageFX.backup.pos.y + y;
            imageFX.updateRect(image);
        }
    }

    @Override
    public void moveFinish() {
        addUndoableStateChange();
    }

    @Override
    public void moveCancel() {
        restoreSelectionState();
    }

    @Override
    public void rotateStart(float x, float y) {
        backupSelectionState();
        updateSelectionCenter();
        dummyVector.set(x, y).sub(selectionCenter).nor();
        rotateStartAngle = dummyVector.angle();
        drawSelectionCenter = true;
    }

    @Override
    public void rotateUpdate(float x, float y) {
        dummyVector.set(x, y).sub(selectionCenter).nor();
        final float rot = dummyVector.angle() - rotateStartAngle;
        final float rotRad = rot * (MathUtils.PI / 180);
        final float cosRotRad = MathUtils.cos(rotRad);
        final float sinRotRad = MathUtils.sin(rotRad);
        for (EcoMapImage image : selection) {
            EcoMapImageFX imageFX = (EcoMapImageFX) image.attachment;
            image.rotation = (imageFX.backup.rotation + rot) % 360;
            final Vector2 p = imageFX.backup.pos;

            image.x = cosRotRad * (p.x - selectionCenter.x)
                    - sinRotRad * (imageFX.backup.pos.y - selectionCenter.y) + selectionCenter.x;
            image.y = sinRotRad * (imageFX.backup.pos.x - selectionCenter.x)
                    + cosRotRad * (imageFX.backup.pos.y - selectionCenter.y) + selectionCenter.y;

            imageFX.updateRect(image);
        }
    }

    @Override
    public void rotateFinish() {
        addUndoableStateChange();
        drawSelectionCenter = false;
    }

    @Override
    public void rotateCancel() {
        restoreSelectionState();
        drawSelectionCenter = false;
    }

    @Override
    public void scaleStart(float x, float y) {
        backupSelectionState();
        updateSelectionCenter();
        dummyVector.set(x, y).sub(selectionCenter);
        scaleStartLength = dummyVector.len();
        drawSelectionCenter = true;
    }

    @Override
    public void scaleUpdate(float x, float y) {
        if (scaleStartLength > 0) {
            dummyVector.set(x, y).sub(selectionCenter);
            float scale = dummyVector.len() / scaleStartLength;
            for (EcoMapImage image : selection) {
                EcoMapImageFX imageFX = (EcoMapImageFX) image.attachment;
                dummyVector.set(imageFX.backup.pos).sub(selectionCenter).scl(scale).add(selectionCenter);
                image.x = dummyVector.x;
                image.y = dummyVector.y;
                image.scale = imageFX.backup.scale * scale;
                if (image.scale < 0.001f) {
                    image.scale = 0.01f;
                }

                imageFX.updateRect(image);
            }
        }
    }

    @Override
    public void scaleFinish() {
        addUndoableStateChange();
        drawSelectionCenter = false;
    }

    @Override
    public void scaleCancel() {
        restoreSelectionState();
        drawSelectionCenter = false;
    }

    public void render(Batch batch, BoundingRect bounds) {
        if (hasSelection()) {
            batch.end();
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.RED);
            if (drawSelectionCenter) {
                float r = 8;
                shapeRenderer.ellipse(selectionCenter.x - r, selectionCenter.y - r, r * 2, r * 2);
                r = 2;
                shapeRenderer.ellipse(selectionCenter.x - r, selectionCenter.y - r, r * 2, r * 2);
            }
            shapeRenderer.setColor(Color.GREEN);
            for (EcoMapImage image : selection) {
                EcoMapImageFX imageFX = (EcoMapImageFX) image.attachment;
                List<EcoPoint> points = imageFX.rect.getPoints();
                EcoPoint last = points.get(points.size() - 1);
                for (EcoPoint point : points) {
                    shapeRenderer.line(last.x, last.y, point.x, point.y);
                    last = point;
                }
            }
            shapeRenderer.end();
            batch.begin();
        }
    }

    private static class ImageStateChange {

        final EcoMapImageFX.State from;
        final EcoMapImageFX.State to;
        private final EcoMapImage image;

        public ImageStateChange(EcoMapImage image) {
            this.image = image;
            EcoMapImageFX imageFX = (EcoMapImageFX) image.attachment;
            from = imageFX.createState();
            from.readFrom(imageFX.backup);
            to = imageFX.createState();
            to.readFrom(image);
        }

        protected void performUndo() {
            from.writeTo(image);
        }

        protected void performRedo() {
            to.writeTo(image);
        }
    }

    private class UndoableStateChange extends UndoableMapEdit {

        final List<ImageStateChange> changes = new ArrayList();

        public UndoableStateChange(MapEditorController controller) {
            super(controller);
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            applyLayerFocus(FocusMode.SELECTION);
            for (ImageStateChange change : changes) {
                change.performUndo();
            }
            controller.getPropertiesEditor().updateValues();
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            applyLayerFocus(FocusMode.SELECTION);
            for (ImageStateChange change : changes) {
                change.performRedo();
            }
            controller.getPropertiesEditor().updateValues();
        }
    }

    private class UndoableSelectionChange extends UndoableMapEdit {

        private final List<EcoMapImage> from;
        private final List<EcoMapImage> to;

        public UndoableSelectionChange(MapEditorController controller,
                List<EcoMapImage> from, List<EcoMapImage> to) {
            super(controller);
            this.from = from;
            this.to = to;
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            focusLayerFX.selection = new ArrayList(from);
            applyLayerFocus(FocusMode.SELECTION);
            onSelectionChange();
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            focusLayerFX.selection = new ArrayList(to);
            applyLayerFocus(FocusMode.SELECTION);
            onSelectionChange();
        }
    }

    private class UndoableDelete extends UndoableMapEdit {

        private final EcoImageLayer layer;
        private final List<EcoMapImage> deleted;

        public UndoableDelete(MapEditorController controller, EcoImageLayer layer,
                List<EcoMapImage> deleted) {
            super(controller);
            this.deleted = deleted;
            this.layer = layer;
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            focusLayerFX.selection = new ArrayList(deleted);
            // fixme: restore correct order?
            for (EcoMapImage image : deleted) {
                layer.images.add(image);
            }
            applyLayerFocus(FocusMode.SELECTION);
            onSelectionChange();
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            for (EcoMapImage image : deleted) {
                layer.images.remove(image);
            }
            focusLayerFX.selection = new ArrayList();
            applyLayerFocus(FocusMode.SELECTION);
            onSelectionChange();
        }
    }
}
