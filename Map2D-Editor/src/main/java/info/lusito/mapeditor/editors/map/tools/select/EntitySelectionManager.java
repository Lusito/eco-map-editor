package info.lusito.mapeditor.editors.map.tools.select;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import info.lusito.mapeditor.editors.map.MapEditorController;
import info.lusito.mapeditor.editors.map.model.EcoMapEntityFX;
import info.lusito.mapeditor.editors.map.model.EcoMapLayerFX;
import info.lusito.mapeditor.persistence.shape.BoundingRect;
import info.lusito.mapeditor.persistence.shape.EcoCircle;
import info.lusito.mapeditor.persistence.shape.EcoPoint;
import info.lusito.mapeditor.editors.map.tools.select.properties.EntityPropertiesGroup;
import info.lusito.mapeditor.editors.map.tools.select.properties.MapEntityComponent;
import info.lusito.mapeditor.editors.map.utils.FocusMode;
import info.lusito.mapeditor.editors.map.utils.UndoableMapEdit;
import info.lusito.mapeditor.editors.properties.api.adapters.PropertiesAdapter;
import info.lusito.mapeditor.persistence.component.EcoComponent;
import info.lusito.mapeditor.persistence.entity.EcoEntityComponent;
import info.lusito.mapeditor.persistence.map.EcoEntityLayer;
import info.lusito.mapeditor.persistence.map.EcoMapEntity;
import info.lusito.mapeditor.persistence.map.EcoMapEntityComponent;
import info.lusito.mapeditor.persistence.shape.EcoPolygon;
import info.lusito.mapeditor.persistence.shape.EcoPolyline;
import info.lusito.mapeditor.persistence.shape.EcoRectangle;
import info.lusito.mapeditor.projecttype.GameProject;
import info.lusito.mapeditor.service.filewatcher.FileWatcher;
import info.lusito.mapeditor.service.filewatcher.WatchedFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.undo.CannotRedoException;

public class EntitySelectionManager implements SelectToolListener {

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private EcoMapLayerFX currentLayerFX;
    private EcoEntityLayer currentLayer;
    private List<EcoMapEntity> selection;
    private final Vector2 selectionCenter = new Vector2();
    private boolean drawSelectionCenter;
    private final Vector2 dummyVector = new Vector2();
    private float rotateStartAngle;
    private float scaleStartLength;
    private final MapEditorController controller;
    private final EntityPropertiesGroup entityProperties;

    public EntitySelectionManager(MapEditorController controller) {
        this.controller = controller;
        entityProperties = new EntityPropertiesGroup(controller);
    }

    public void setFocusLayer(EcoMapLayerFX layerFX, EcoEntityLayer currentLayer, boolean refocusSelection) {
        this.currentLayerFX = layerFX;
        this.currentLayer = currentLayer;
        selection = layerFX == null ? null : layerFX.selection;
        if(selection != null && refocusSelection) {
            onSelectionChange();
        }
    }

    private void addUndoableSelectionChange(List<EcoMapEntity> from, List<EcoMapEntity> to) {
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
            List<EcoMapEntity> backup = selection;
            setSelection(new ArrayList());
            addUndoableSelectionChange(backup, selection);
        }
    }

    private void setSelection(List<EcoMapEntity> list) {
        selection = list;
        currentLayerFX.selection = list;
    }

    private List<EcoMapEntity> duplicateSelection() {
        List<EcoMapEntity> old = selection;
        setSelection(new ArrayList(selection));
        return old;
    }

    private void onSelectionChange() {
        PropertiesAdapter editor = controller.getPropertiesEditor();
        editor.groups.clear();
        editor.loaded = true;
        editor.title = "Entity";
        editor.instance = true;
        if (selection != null && selection.size() == 1) {
            final EcoMapEntity entity = selection.get(0);
            entityProperties.setEntity(entity);
            editor.groups.add(entityProperties);
            EcoMapEntityFX entityFX = (EcoMapEntityFX) entity.attachment;
            FileWatcher xcdFiles = ((GameProject) controller.getProject()).getFileWatcher("xcd");
            if (entityFX.groups == null) {
                entityFX.groups = new CopyOnWriteArrayList();
                for (EcoEntityComponent entityComponent : entityFX.definition.components) {
                    final WatchedFile file = xcdFiles.getFile(entityComponent.src);
                    if (file != null) {
                        EcoMapEntityComponent mapEntityComponent = entity.findComponent(entityComponent.src, true);
                        EcoComponent component = (EcoComponent) file.getContent();
                        entityFX.groups.add(new MapEntityComponent(controller,
                                component, entityComponent, mapEntityComponent));
                    }
                }
            }
            editor.groups.addAll(entityFX.groups);
        }
        editor.updateEverything();
    }

    @Override
    public boolean testSelection(EcoCircle circle) {
        if (selection != null) {
            for (EcoMapEntity entity : selection) {
                if (((EcoMapEntityFX) entity.attachment).rect.intersects(circle)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean setMoveSelection(EcoCircle circle) {
        EcoMapEntity entity = find(circle);
        if (entity == null) {
            clearSelection();
            return false;
        }

        List<EcoMapEntity> backup = selection;
        setSelection(new ArrayList());
        selection.add(entity);
        addUndoableSelectionChange(backup, selection);
        return true;
    }

    @Override
    public void touch(EcoCircle circle) {
        EcoMapEntity entity = find(circle);
        if (hasSelection()) {
            if (entity == null) {
                clearSelection();
            } else if (!selection.contains(entity)) {
                List<EcoMapEntity> backup = selection;
                setSelection(new ArrayList());
                selection.add(entity);
                addUndoableSelectionChange(backup, selection);
            }
        } else if (entity != null) {
            List<EcoMapEntity> backup = duplicateSelection();
            selection.add(entity);
            addUndoableSelectionChange(backup, selection);
        }
    }

    public EcoMapEntity find(EcoCircle circle) {
        if (currentLayer != null) {
            for (int i = currentLayer.entities.size() - 1; i >= 0; i--) {
                EcoMapEntity entity = currentLayer.entities.get(i);
                if (((EcoMapEntityFX) entity.attachment).rect.intersects(circle)) {
                    return entity;
                }
            }
        }
        return null;
    }

    @Override
    public void addSelection(BoundingRect rect) {
        if (currentLayer != null) {
            List<EcoMapEntity> backup = null;
            for (EcoMapEntity entity : currentLayer.entities) {
                if (!selection.contains(entity) && intersects(entity, rect)) {
                    if (backup == null) {
                        backup = duplicateSelection();
                    }
                    selection.add(entity);
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
            List<EcoMapEntity> backup = null;
            for (EcoMapEntity entity : currentLayer.entities) {
                if (selection.contains(entity) && intersects(entity, rect)) {
                    if (backup == null) {
                        backup = duplicateSelection();
                    }
                    selection.remove(entity);
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
            List<EcoMapEntity> backup = null;
            for (EcoMapEntity entity : currentLayer.entities) {
                if (intersects(entity, rect)) {
                    if (backup == null) {
                        backup = duplicateSelection();
                    }
                    if (selection.contains(entity)) {
                        selection.remove(entity);
                    } else {
                        selection.add(entity);
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
        for (EcoMapEntity entity : selection) {
            selectionCenter.x += entity.x;
            selectionCenter.y += entity.y;
            count++;
        }
        if (count > 0) {
            selectionCenter.scl(1 / count);
        }
    }

    private void backupSelectionState() {
        for (EcoMapEntity entity : selection) {
            EcoMapEntityFX entityFX = (EcoMapEntityFX) entity.attachment;
            entityFX.backup.readFrom(entity);
        }
    }

    private void restoreSelectionState() {
        for (EcoMapEntity entity : selection) {
            EcoMapEntityFX entityFX = (EcoMapEntityFX) entity.attachment;
            entityFX.backup.writeTo(entity);
        }
    }

    private void addUndoableStateChange() {
        UndoableStateChange entry = new UndoableStateChange(controller);
        for (EcoMapEntity entity : selection) {
            entry.changes.add(new EntityStateChange(entity));
        }
        controller.addUndoableEdit(entry);
        controller.getPropertiesEditor().updateValues();
    }

    private static boolean intersects(EcoMapEntity entity, BoundingRect rect) {
        EcoMapEntityFX entityFX = (EcoMapEntityFX) entity.attachment;
        return rect.intersects(entityFX.rect);
    }

    @Override
    public void invertSelection() {
        if (currentLayer.entities.isEmpty()) {
            selection.clear(); // should be clear, but just in case.
        } else {
            List<EcoMapEntity> newSelection = new ArrayList();
            for (EcoMapEntity entity : currentLayer.entities) {
                if (!selection.contains(entity)) {
                    newSelection.add(entity);
                }
            }
            List<EcoMapEntity> backup = selection;
            setSelection(newSelection);
            addUndoableSelectionChange(backup, selection);
        }
    }

    @Override
    public void selectAll() {
        List<EcoMapEntity> backup = selection;
        setSelection(new ArrayList(currentLayer.entities));
        addUndoableSelectionChange(backup, selection);
    }

    @Override
    public void deleteSelection() {
        controller.addUndoableEdit(new UndoableDelete(controller, currentLayer, selection));
        for (EcoMapEntity entity : selection) {
            currentLayer.entities.remove(entity);
        }
        setSelection(new ArrayList());
    }

    @Override
    public void moveStart() {
        backupSelectionState();
    }

    @Override
    public void moveUpdate(float x, float y) {
        for (EcoMapEntity entity : selection) {
            EcoMapEntityFX entityFX = (EcoMapEntityFX) entity.attachment;
            entity.x = entityFX.backup.pos.x + x;
            entity.y = entityFX.backup.pos.y + y;
            if(entity.shape != null) {
                switch(entity.shape.getType()) {
                    case CIRCLE:
                        moveUpdate((EcoCircle)entity.shape, (EcoCircle)entityFX.backup.shape, x, y);
                        break;
                    case RECTANGLE:
                        EcoRectangle rect = (EcoRectangle)entity.shape;
                        EcoRectangle rectB = (EcoRectangle)entityFX.backup.shape;
                        moveUpdate(rect.getPoints(), rectB.getPoints(), x, y);
                        break;
                    case POLYGON:
                        EcoPolygon polygon = (EcoPolygon)entity.shape;
                        EcoPolygon polygonB = (EcoPolygon)entityFX.backup.shape;
                        moveUpdate(polygon.points, polygonB.points, x, y);
                        moveUpdate(polygon.bounds, polygonB.bounds, x, y);
                        break;
                    case POLYLINE:
                        EcoPolyline polyline = (EcoPolyline)entity.shape;
                        EcoPolyline polylineB = (EcoPolyline)entityFX.backup.shape;
                        moveUpdate(polyline.points, polylineB.points, x, y);
                        moveUpdate(polyline.bounds, polyline.bounds, x, y);
                        break;
                }
            }
            entityFX.updateRect(entity);
        }
    }

    private void moveUpdate(EcoCircle a, EcoCircle b, float x, float y) {
        a.center.x = b.center.x + x;
        a.center.y = b.center.y + y;
    }
    
    public void moveUpdate(List<EcoPoint> a, List<EcoPoint> b, float x, float y) {
        assert(a.size() == b.size());
        for (int i = 0; i < a.size(); i++) {
            EcoPoint ap = a.get(i);
            EcoPoint bp = b.get(i);
            ap.x = bp.x + x;
            ap.y = bp.y + y;
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
        for (EcoMapEntity entity : selection) {
            EcoMapEntityFX entityFX = (EcoMapEntityFX) entity.attachment;
            entity.rotation = (entityFX.backup.rotation + rot) % 360;
            final Vector2 p = entityFX.backup.pos;

            entity.x = cosRotRad * (p.x - selectionCenter.x)
                    - sinRotRad * (p.y - selectionCenter.y) + selectionCenter.x;
            entity.y = sinRotRad * (p.x - selectionCenter.x)
                    + cosRotRad * (p.y - selectionCenter.y) + selectionCenter.y;

            if(entity.shape != null) {
                switch(entity.shape.getType()) {
                    case CIRCLE:
                        rotateUpdate(
                                ((EcoCircle)entity.shape).center,
                                ((EcoCircle)entityFX.backup.shape).center,
                                cosRotRad, sinRotRad);
                        break;
                    case RECTANGLE:
                        EcoRectangle rect = (EcoRectangle)entity.shape;
                        EcoRectangle rectB = (EcoRectangle)entityFX.backup.shape;
                        rotateUpdate(rect.getPoints(), rectB.getPoints(), cosRotRad, sinRotRad);
                        break;
                    case POLYGON:
                        EcoPolygon polygon = (EcoPolygon)entity.shape;
                        EcoPolygon polygonB = (EcoPolygon)entityFX.backup.shape;
                        rotateUpdate(polygon.points, polygonB.points, cosRotRad, sinRotRad);
                        rotateUpdate(polygon.bounds.center, polygonB.bounds.center, cosRotRad, sinRotRad);
                        break;
                    case POLYLINE:
                        EcoPolyline polyline = (EcoPolyline)entity.shape;
                        EcoPolyline polylineB = (EcoPolyline)entityFX.backup.shape;
                        rotateUpdate(polyline.points, polylineB.points, cosRotRad, sinRotRad);
                        rotateUpdate(polyline.bounds.center, polyline.bounds.center, cosRotRad, sinRotRad);
                        break;
                }
            }
            entityFX.updateRect(entity);
        }
    }

    private void rotateUpdate(final EcoPoint a, final EcoPoint b, float cosRotRad, float sinRotRad) {
        a.x = cosRotRad * (b.x - selectionCenter.x)
                - sinRotRad * (b.y - selectionCenter.y) + selectionCenter.x;
        a.y = sinRotRad * (b.x - selectionCenter.x)
                + cosRotRad * (b.y - selectionCenter.y) + selectionCenter.y;
    }

    private void rotateUpdate(List<EcoPoint> a, List<EcoPoint> b, float cosRotRad, float sinRotRad) {
        assert(a.size() == b.size());
        for (int i = 0; i < a.size(); i++) {
            rotateUpdate(a.get(i), b.get(i), cosRotRad, sinRotRad);
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
            for (EcoMapEntity entity : selection) {
                EcoMapEntityFX entityFX = (EcoMapEntityFX) entity.attachment;
                dummyVector.set(entityFX.backup.pos).sub(selectionCenter).scl(scale).add(selectionCenter);
                entity.x = dummyVector.x;
                entity.y = dummyVector.y;
                entity.scale = entityFX.backup.scale * scale;
                if (entity.scale < 0.001f) {
                    entity.scale = 0.01f;
                }

                if(entity.shape != null) {
                    switch(entity.shape.getType()) {
                        case CIRCLE:
                            scaleUpdate((EcoCircle)entity.shape, (EcoCircle)entityFX.backup.shape, scale);
                            break;
                        case RECTANGLE:
                            EcoRectangle rect = (EcoRectangle)entity.shape;
                            EcoRectangle rectB = (EcoRectangle)entityFX.backup.shape;
                            scaleUpdate(rect.getPoints(), rectB.getPoints(), scale);
                            break;
                        case POLYGON:
                            EcoPolygon polygon = (EcoPolygon)entity.shape;
                            EcoPolygon polygonB = (EcoPolygon)entityFX.backup.shape;
                            scaleUpdate(polygon.points, polygonB.points, scale);
                            scaleUpdate(polygon.bounds, polygonB.bounds, scale);
                            break;
                        case POLYLINE:
                            EcoPolyline polyline = (EcoPolyline)entity.shape;
                            EcoPolyline polylineB = (EcoPolyline)entityFX.backup.shape;
                            scaleUpdate(polyline.points, polylineB.points, scale);
                            scaleUpdate(polyline.bounds, polyline.bounds, scale);
                            break;
                    }
                }
                entityFX.updateRect(entity);
            }
        }
    }

    private void scaleUpdate(EcoCircle a, EcoCircle b, float scale) {
        dummyVector.set(b.center.x, b.center.y).sub(selectionCenter).scl(scale).add(selectionCenter);
        a.center.x = dummyVector.x;
        a.center.y = dummyVector.y;
        a.radius = b.radius * scale;
    }

    private void scaleUpdate(List<EcoPoint> a, List<EcoPoint> b, float scale) {
        assert(a.size() == b.size());
        for (int i = 0; i < a.size(); i++) {
            EcoPoint ap = a.get(i);
            EcoPoint bp = b.get(i);
            dummyVector.set(bp.x, bp.y).sub(selectionCenter).scl(scale).add(selectionCenter);
            ap.x = dummyVector.x;
            ap.y = dummyVector.y;
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
            for (EcoMapEntity entity : selection) {
                EcoMapEntityFX entityFX = (EcoMapEntityFX) entity.attachment;
                List<EcoPoint> points = entityFX.rect.getPoints();
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

    private static class EntityStateChange {

        final EcoMapEntityFX.State from;
        final EcoMapEntityFX.State to;
        private final EcoMapEntity entity;

        public EntityStateChange(EcoMapEntity entity) {
            this.entity = entity;
            EcoMapEntityFX entityFX = (EcoMapEntityFX) entity.attachment;
            from = entityFX.createState();
            from.readFrom(entityFX.backup);
            to = entityFX.createState();
            to.readFrom(entity);
        }

        protected void performUndo() {
            from.writeTo(entity);
        }

        protected void performRedo() {
            to.writeTo(entity);
        }
    }

    private class UndoableStateChange extends UndoableMapEdit {

        final List<EntityStateChange> changes = new ArrayList();

        public UndoableStateChange(MapEditorController controller) {
            super(controller);
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            applyLayerFocus(FocusMode.SELECTION);
            for (EntityStateChange change : changes) {
                change.performUndo();
            }
            controller.getPropertiesEditor().updateValues();
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            applyLayerFocus(FocusMode.SELECTION);
            for (EntityStateChange change : changes) {
                change.performRedo();
            }
            controller.getPropertiesEditor().updateValues();
        }
    }

    private class UndoableSelectionChange extends UndoableMapEdit {

        private final List<EcoMapEntity> from;
        private final List<EcoMapEntity> to;

        public UndoableSelectionChange(MapEditorController controller,
                List<EcoMapEntity> from, List<EcoMapEntity> to) {
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

        private final EcoEntityLayer layer;
        private final List<EcoMapEntity> deleted;

        public UndoableDelete(MapEditorController controller, EcoEntityLayer layer,
                List<EcoMapEntity> deleted) {
            super(controller);
            this.deleted = deleted;
            this.layer = layer;
        }

        @Override
        protected void performUndo() throws CannotRedoException {
            focusLayerFX.selection = new ArrayList(deleted);
            // fixme: restore correct order?
            for (EcoMapEntity entity : deleted) {
                layer.entities.add(entity);
            }
            applyLayerFocus(FocusMode.SELECTION);
            onSelectionChange();
        }

        @Override
        protected void performRedo() throws CannotRedoException {
            for (EcoMapEntity entity : deleted) {
                layer.entities.remove(entity);
            }
            focusLayerFX.selection = new ArrayList();
            applyLayerFocus(FocusMode.SELECTION);
            onSelectionChange();
        }
    }
}
