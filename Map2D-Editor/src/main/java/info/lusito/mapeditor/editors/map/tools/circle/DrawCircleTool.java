package info.lusito.mapeditor.editors.map.tools.circle;

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
import info.lusito.mapeditor.persistence.shape.EcoCircle;
import info.lusito.mapeditor.persistence.shape.EcoShape;

public class DrawCircleTool extends AbstractTool {

    protected final DrawCircleInputHandler input = new DrawCircleInputHandler(this);
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Vector2 start = new Vector2();
    private final Vector2 end = new Vector2();
    private boolean dragging;
    private float radius;
    private EcoMapLayerFX focusLayer;

    public DrawCircleTool(SimpleCamera simpleCamera, EcoMapFX mapFX) {
        super(simpleCamera, mapFX, createButton("circle", "Draw a circle", "Draw a circle"));
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

    void start(int screenX, int screenY) {
        if(focusLayer.selection.size() == 1) {
            dragging = true;
            radius = 0;
            // convert to map coordinates
            start.set(screenX, screenY);
            camera.unproject(start);
        } else {
            mapFX.getController().showSingleEntitySelectionRequirement();
        }
    }

    void update(int screenX, int screenY) {
        if(dragging) {
            // convert to map coordinates
            end.set(screenX, screenY);
            camera.unproject(end);
            radius = start.dst(end);
        }
    }

    void stop(int screenX, int screenY, boolean accept) {
        if(accept) {
            update(screenX, screenY);
            if(focusLayer.selection.size() == 1) {
                EcoMapEntity entity = (EcoMapEntity)focusLayer.selection.get(0);
                float oldX = entity.x;
                float oldY = entity.y;
                EcoShape oldShape = entity.shape;
                final EcoCircle circle = new EcoCircle();
                circle.center.set(start.x, start.y);
                circle.radius = radius;
                entity.shape = circle;
                entity.x = start.x;
                entity.y = start.y;
                EcoMapEntityFX entityFX = (EcoMapEntityFX) entity.attachment;
                entityFX.updateRect(entity);
                mapFX.getController().addUndoableShapeChange(entity, oldX, oldY, oldShape);
            } else {
                mapFX.getController().showSingleEntitySelectionRequirement();
            }
        }
        dragging = false;
    }

    public void render(SpriteBatch batch, float zoom) {
        if(dragging && !input.isAborted() && focusLayer.selection.size() == 1) {
            batch.end();
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            EcoMapEntity entity = (EcoMapEntity)focusLayer.selection.get(0);
            EcoMapEntityGDX entityFX = (EcoMapEntityGDX) entity.attachment;
            shapeRenderer.setColor(entityFX.shapeColor);
            shapeRenderer.circle(start.x, start.y, radius);
            shapeRenderer.end();
            batch.begin();
        }
    }

    public void setFocusLayer(EcoMapLayerFX layer) {
        focusLayer = layer;
    }
}
