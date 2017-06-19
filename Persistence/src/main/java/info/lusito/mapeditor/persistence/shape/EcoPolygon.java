package info.lusito.mapeditor.persistence.shape;

import java.util.ArrayList;
import java.util.List;

public class EcoPolygon extends EcoShape {

    private final static EcoPoint OUTSIDE = new EcoPoint();
    public final EcoCircle bounds = new EcoCircle();
    public final List<EcoPoint> points;

    public EcoPolygon() {
        points = new ArrayList();
    }

    public EcoPolygon(EcoPolygon other) {
        points = ShapeUtil.copyPointList(other.points);
        bounds.set(other.bounds);
    }

    public EcoPolygon(List<EcoPoint> points) {
        this.points = points;
        updateBounds();
    }

    public EcoPolygon(String data) {
        points = ShapeUtil.dataToPointsList(data);
        updateBounds();
    }

    @Override
    public Type getType() {
        return Type.POLYGON;
    }

    @Override
    public EcoCircle getBounds() {
        return bounds;
    }

    @Override
    public EcoShape copy() {
        return new EcoPolygon(this);
    }

    public final void updateBounds() {
        ShapeUtil.updateBounds(bounds, points);
    }
    
    public boolean containsPoint(EcoPoint p) {
        if (points.size() < 3 || !bounds.contains(p)) {
            return false;
        }
        int hits = 0;
        OUTSIDE.set(bounds.center.x, bounds.center.y + bounds.radius*1.1f);
        int size = points.size();
        EcoPoint last = points.get(size-1);
        for (EcoPoint point : points) {
            if(ShapeUtil.linesIntersect(bounds.center, OUTSIDE, last, point))
                hits++;
            last = point;
        }
        return ((hits & 1) != 0);
    }

    public boolean intersects(EcoCircle circle) {
        if (points.isEmpty()) {
            return false;
        }
        for (EcoPoint point : points) {
            if (circle.contains(point)) {
                return true;
            }
        }
        final int size = points.size();
        if (size == 1 || !bounds.intersects(circle)) {
            return false;
        }
        if (containsPoint(circle.center)) {
            return true;
        }

        int max = size - 1;
        for (int i = 0; i < max; i++) {
            if (circle.intersectsLine(points.get(i), points.get(i + 1))) {
                return true;
            }
        }
        return circle.intersectsLine(points.get(max), points.get(0));
    }

    public boolean intersects(EcoRectangle rect) {
        if (!rect.bounds.intersects(bounds)) {
            return false;
        }
        for (EcoPoint point : points) {
            if (rect.containsPoint(point)) {
                return true;
            }
        }
        if (containsPoint(rect.a) || containsPoint(rect.b)
                || containsPoint(rect.c) || containsPoint(rect.d)) {
            return true;
        }

        int max = points.size() - 1;
        for (int i = 0; i < max; i++) {
            if (rect.intersectsLine(points.get(i), points.get(i + 1))) {
                return true;
            }
        }
        return rect.intersectsLine(points.get(max), points.get(0));
    }

    public String toData() {
        return ShapeUtil.pointListDoData(points);
    }
}
