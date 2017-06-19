package info.lusito.mapeditor.persistence.shape;

import java.util.ArrayList;
import java.util.List;

public class EcoPolyline extends EcoShape {

    public final EcoCircle bounds = new EcoCircle();
    public final List<EcoPoint> points;

    public EcoPolyline() {
        points = new ArrayList();
    }

    public EcoPolyline(EcoPolyline other) {
        points = ShapeUtil.copyPointList(other.points);
        bounds.set(other.bounds);
    }

    public EcoPolyline(List<EcoPoint> points) {
        this.points = points;
        updateBounds();
    }

    public EcoPolyline(String data) {
        points = ShapeUtil.dataToPointsList(data);
        updateBounds();
    }

    @Override
    public Type getType() {
        return Type.POLYLINE;
    }

    @Override
    public EcoCircle getBounds() {
        return bounds;
    }

    @Override
    public EcoShape copy() {
        return new EcoPolyline(this);
    }

    public final void updateBounds() {
        ShapeUtil.updateBounds(bounds, points);
    }

    public boolean intersects(EcoCircle circle) {
        if (points.isEmpty()) {
            return false;
        }
        final int size = points.size();
        for (EcoPoint point : points) {
            if (circle.contains(point)) {
                return true;
            }
        }
        if (size == 1) {
            return false;
        }
        if (!bounds.intersects(circle)) {
            return false;
        }

        int max = size - 1;
        for (int i = 0; i < max; i++) {
            if (circle.intersectsLine(points.get(i), points.get(i + 1))) {
                return true;
            }
        }
        return false;
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

        int max = points.size() - 1;
        for (int i = 0; i < max; i++) {
            if (rect.intersectsLine(points.get(i), points.get(i + 1))) {
                return true;
            }
        }
        return false;
    }

    public String toData() {
        return ShapeUtil.pointListDoData(points);
    }
}
