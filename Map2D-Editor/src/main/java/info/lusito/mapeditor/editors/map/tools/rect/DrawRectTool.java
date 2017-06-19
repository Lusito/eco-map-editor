package info.lusito.mapeditor.editors.map.tools.rect;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
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
import info.lusito.mapeditor.persistence.shape.EcoRectangle;
import info.lusito.mapeditor.persistence.shape.EcoShape;

public class DrawRectTool extends AbstractTool {

    protected final DrawRectInputHandler input = new DrawRectInputHandler(this);
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Vector2 start = new Vector2();
    private final Vector2 end = new Vector2();
    private boolean dragging;
    private float width;
    private float height;
    private EcoMapLayerFX focusLayer;

    public DrawRectTool(SimpleCamera simpleCamera, EcoMapFX mapFX) {
        super(simpleCamera, mapFX, createButton("rect", "Draw a rectangle", "Draw a rectangle"));
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
            width = height = 0;
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
            width = end.x - start.x;
            height = end.y - start.y;
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
                final EcoRectangle rect = new EcoRectangle();
                if(width < 0) {
                    start.x += width;
                    width *= -1;
                }
                if(height < 0) {
                    start.y += height;
                    height *= -1;
                }
                start.x += width/2;
                start.y += height/2;
                rect.set(start.x, start.y, width, height, 0);
                entity.shape = rect;
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
            shapeRenderer.rect(start.x, start.y, width, height);
            shapeRenderer.end();
            batch.begin();
        }
    }

    public void setFocusLayer(EcoMapLayerFX layer) {
        focusLayer = layer;
    }
}
