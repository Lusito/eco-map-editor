package info.lusito.mapeditor.renderers;

import info.lusito.mapeditor.model.EcoMapEntityGDX;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import info.lusito.mapeditor.persistence.map.EcoEntityLayer;
import info.lusito.mapeditor.persistence.map.EcoMap;
import info.lusito.mapeditor.persistence.map.EcoMapEntity;
import info.lusito.mapeditor.persistence.map.EcoMapLayer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import info.lusito.mapeditor.persistence.shape.BoundingRect;
import info.lusito.mapeditor.persistence.shape.EcoCircle;
import info.lusito.mapeditor.persistence.shape.EcoPoint;
import info.lusito.mapeditor.persistence.shape.EcoPolygon;
import info.lusito.mapeditor.persistence.shape.EcoPolyline;
import info.lusito.mapeditor.persistence.shape.EcoRectangle;
import java.util.List;

public class EcoEntityLayerRendererGDX implements EcoMapLayerRendererGDX {

    private final Vector2 dummyVector = new Vector2();
    final ShapeRenderer shapeRenderer = new ShapeRenderer();
    final EcoMap map;

    public EcoEntityLayerRendererGDX(EcoMap map) {
        this.map = map;
    }

    @Override
    public void render(Batch batch, EcoMapLayer layer, BoundingRect bounds) {
        render(batch, (EcoEntityLayer) layer, bounds);
    }

    private void render(Batch batch, EcoEntityLayer layer, BoundingRect bounds) {
        Gdx.gl20.glLineWidth(2);

        boolean hasShapes = false;
        for (EcoMapEntity entity : layer.entities) {
            if(entity.shape != null)
                hasShapes = true;
            if (entity.attachment == null) {
                continue;
            }
            EcoMapEntityGDX entityGDX = (EcoMapEntityGDX) entity.attachment;
            if (isVisible(entityGDX, bounds)) {
                Texture texture = entityGDX.texture;
                final float width = texture.getWidth() * entity.scale;
                float halfWidth = width * 0.5f;
                final float height = texture.getHeight() * entity.scale;
                float halfHeight = height * 0.5f;
                batch.draw(texture, entity.x - halfWidth, entity.y - halfHeight, halfWidth, halfHeight,
                        width, height, 1, 1, entity.rotation,
                        0, 0, texture.getWidth(), texture.getHeight(), false, true);
            }
        }
        if(hasShapes) {
            batch.end();
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            for (EcoMapEntity entity : layer.entities) {
                if (entity.shape == null) {
                    continue;
                }
                if (bounds.intersects(entity.shape.getBounds())) {
                    EcoMapEntityGDX entityFX = (EcoMapEntityGDX) entity.attachment;
                    shapeRenderer.setColor(entityFX.shapeColor);
                    switch(entity.shape.getType()) {
                        case CIRCLE:
                            EcoCircle circle = (EcoCircle)entity.shape;
                            shapeRenderer.circle(circle.center.x, circle.center.y, circle.radius);
                            break;
                        case RECTANGLE:
                            drawPolyline(((EcoRectangle)entity.shape).getPoints(), true);
                            break;
                        case POLYGON:
                            drawPolyline(((EcoPolygon)entity.shape).points, true);
                            break;
                        case POLYLINE:
                            drawPolyline(((EcoPolyline)entity.shape).points, false);
                            break;
                    }
                }
            }
            shapeRenderer.end();
            batch.begin();
        }
    }

    private void drawPolyline(final List<EcoPoint> points, boolean enclosed) {
        if(points.size() >= 2) {
            EcoPoint last = enclosed ? points.get(points.size()-1) : null;
            for (EcoPoint point : points) {
                if(last != null)
                    shapeRenderer.line(last.x, last.y, point.x, point.y);
                last = point;
            }
            if(!enclosed) {
                last = points.get(points.size()-1);
                EcoPoint prev = points.get(points.size()-2);
                dummyVector.set(prev.x-last.x, prev.y-last.y);
                drawArrow(last, dummyVector);
                EcoPoint first = points.get(0);
                EcoPoint next = points.get(1);
                dummyVector.set(first.x-next.x, first.y-next.y);
                drawArrow(first, dummyVector);
            }
        }
    }

    private void drawArrow(EcoPoint point, Vector2 direction) {
        direction.nor();
        float x = point.x +direction.x*8;
        float y = point.y +direction.y*8;
        direction.rotate(90);
        shapeRenderer.line(point.x, point.y, x + direction.x*6, y + direction.y*6);
        shapeRenderer.line(point.x, point.y, x - direction.x*6, y - direction.y*6);
    }

    private boolean isVisible(EcoMapEntityGDX entityGDX, BoundingRect bounds) {
        return bounds.intersects(entityGDX.rect);
    }
}
