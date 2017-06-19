package info.lusito.mapeditor.editors.map.tools.polyline;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import info.lusito.mapeditor.editors.map.model.EcoMapEntityFX;
import info.lusito.mapeditor.sharedlibgdx.camera.SimpleCamera;
import info.lusito.mapeditor.editors.map.model.EcoMapFX;
import info.lusito.mapeditor.editors.map.model.EcoMapLayerFX;
import info.lusito.mapeditor.editors.map.tools.AbstractTool;
import info.lusito.mapeditor.model.EcoMapEntityGDX;
import info.lusito.mapeditor.persistence.map.EcoMapEntity;
import info.lusito.mapeditor.persistence.shape.EcoPoint;
import info.lusito.mapeditor.persistence.shape.EcoPolygon;
import info.lusito.mapeditor.persistence.shape.EcoPolyline;
import info.lusito.mapeditor.persistence.shape.EcoShape;
import java.util.ArrayList;
import java.util.List;

public class DrawPolylineTool extends AbstractTool {

    protected final DrawPolylineInputHandler input = new DrawPolylineInputHandler(this);
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private List<EcoPoint> points = new ArrayList();
    private final EcoPoint draggedPoint = new EcoPoint();
    private final Vector2 dummyVector = new Vector2();
    private boolean drawing;
    private EcoMapLayerFX focusLayer;
    private final boolean enclosed;
    private boolean dragging;

    public DrawPolylineTool(SimpleCamera simpleCamera, EcoMapFX mapFX, boolean enclosed) {
        super(simpleCamera, mapFX, createButton(
                enclosed ? "polygon" : "polyline",
                enclosed ? "Draw a polygon" : "Draw a polyline",
                enclosed ? "Draw a polygon" : "Draw a polyline"
        ));
        this.enclosed = enclosed;
    }

    public InputProcessor getInput() {
        return input;
    }

    @Override
    public void select() {
        super.select();
        if(focusLayer.selection.size() != 1) {
            mapFX.getController().showSingleEntitySelectionRequirement();
        }
    }

    @Override
    public void deselect() {
        super.deselect();
        drawing = false;
        points.clear();
    }

    void add(int screenX, int screenY) {
        if(focusLayer.selection.size() == 1) {
        } else {
            mapFX.getController().showSingleEntitySelectionRequirement();
        }
        if(focusLayer.selection.size() == 1) {
            if(!drawing) {
                drawing = true;
                points.clear();
            }
            // convert to map coordinates
            dummyVector.set(screenX, screenY);
            camera.unproject(dummyVector);
            points.add(new EcoPoint(dummyVector.x, dummyVector.y));
            dragging = false;
        } else {
            mapFX.getController().showSingleEntitySelectionRequirement();
        }
    }

    void update(int screenX, int screenY) {
        dragging = true;
        // convert to map coordinates
        dummyVector.set(screenX, screenY);
        camera.unproject(dummyVector);
        draggedPoint.set(dummyVector.x, dummyVector.y);
    }

    void stop(int screenX, int screenY, boolean accept) {
        if(accept) {
            if(focusLayer.selection.size() == 1) {
                //Fixme: not if less than 2 points
                EcoMapEntity entity = (EcoMapEntity)focusLayer.selection.get(0);
                float oldX = entity.x;
                float oldY = entity.y;
                EcoShape oldShape = entity.shape;
                entity.shape = enclosed ? new EcoPolygon(points) : new EcoPolyline(points);
                EcoPoint first = points.get(0);
                entity.x = first.x;
                entity.y = first.y;
                EcoMapEntityFX entityFX = (EcoMapEntityFX) entity.attachment;
                entityFX.updateRect(entity);
                points = new ArrayList();
                mapFX.getController().addUndoableShapeChange(entity, oldX, oldY, oldShape);
            } else {
                mapFX.getController().showSingleEntitySelectionRequirement();
            }
        }
        drawing = false;
        dragging = false;
    }

    public void render(SpriteBatch batch, float zoom) {
        if (drawing && !input.isAborted() && points.size() > 0 && focusLayer.selection.size() == 1) {
            batch.end();
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            EcoMapEntity entity = (EcoMapEntity)focusLayer.selection.get(0);
            EcoMapEntityGDX entityFX = (EcoMapEntityGDX) entity.attachment;
            shapeRenderer.setColor(entityFX.shapeColor);
            EcoPoint first = points.get(0);
            shapeRenderer.circle(first.x, first.y, 4);
            if (points.size() > 1) {
                EcoPoint prev = null;
                if (enclosed) {
                    prev = dragging ? draggedPoint : points.get(points.size() - 1);
                }
                for (EcoPoint point : points) {
                    if (prev != null) {
                        shapeRenderer.line(prev.x, prev.y, point.x, point.y);
                    }
                    prev = point;
                }
                if (dragging) {
                    shapeRenderer.line(prev.x, prev.y, draggedPoint.x, draggedPoint.y);
                }
            } else if(dragging) {
                shapeRenderer.line(first.x, first.y, draggedPoint.x, draggedPoint.y);
            }
            shapeRenderer.end();
            batch.begin();
        }
    }

    public void setFocusLayer(EcoMapLayerFX layer) {
        focusLayer = layer;
    }
}
