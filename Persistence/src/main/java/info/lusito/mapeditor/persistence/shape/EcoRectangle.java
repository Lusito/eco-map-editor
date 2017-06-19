package info.lusito.mapeditor.persistence.shape;

import java.util.ArrayList;
import java.util.List;

public class EcoRectangle extends EcoShape {

    private static final EcoPoint RIGHT = new EcoPoint();
    private static final EcoPoint DOWN = new EcoPoint();

    protected final EcoCircle bounds = new EcoCircle();
    protected final List<EcoPoint> points;
    protected final EcoPoint a;
    protected final EcoPoint b;
    protected final EcoPoint c;
    protected final EcoPoint d;


    public EcoRectangle(EcoRectangle other) {
        points = new ArrayList(4);
        points.add(a = new EcoPoint(other.a));
        points.add(b = new EcoPoint(other.b));
        points.add(c = new EcoPoint(other.c));
        points.add(d = new EcoPoint(other.d));
        bounds.set(other.bounds);
    }

    public EcoRectangle() {
        points = new ArrayList(4);
        points.add(a = new EcoPoint());
        points.add(b = new EcoPoint());
        points.add(c = new EcoPoint());
        points.add(d = new EcoPoint());
    }

    public EcoRectangle(String data) {
        points = ShapeUtil.dataToPointsList(data);
        assert (points.size() == 4);
        a = points.get(0);
        b = points.get(1);
        c = points.get(2);
        d = points.get(3);

        updateBounds();
    }

    @Override
    public Type getType() {
        return Type.RECTANGLE;
    }

    @Override
    public EcoCircle getBounds() {
        return bounds;
    }

    @Override
    public EcoShape copy() {
        return new EcoRectangle(this);
    }

    public float getWidth() {
        float dx = b.x - a.x;
        float dy = b.y - a.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public float getHeight() {
        float dx = d.x - a.x;
        float dy = d.y - a.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public float getAngle() {
        float dx = b.x - a.x;
        float dy = b.y - a.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len != 0) {
            dx /= len;
            dy /= len;
        }
        float angle = (float) (Math.atan2(dy, dx) * 180f / Math.PI);
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    public List<EcoPoint> getPoints() {
        return points;
    }

    public void set(float x, float y, float width, float height, float angle) {
        float hw = width / 2;
        float hh = height / 2;
        RIGHT.set(angle);
        RIGHT.x *= hw;
        RIGHT.y *= hw;
        DOWN.set(angle - 90);
        DOWN.x *= hh;
        DOWN.y *= hh;

        a.set(x - RIGHT.x - DOWN.x, y - RIGHT.y - DOWN.y);
        b.set(x + RIGHT.x - DOWN.x, y + RIGHT.y - DOWN.y);
        c.set(x + RIGHT.x + DOWN.x, y + RIGHT.y + DOWN.y);
        d.set(x - RIGHT.x + DOWN.x, y - RIGHT.y + DOWN.y);
        updateBounds();
    }

    public final void updateBounds() {
        float dx = (c.x - a.x)/2;
        float dy = (c.y - a.y)/2;
        bounds.center.x = a.x + dx;
        bounds.center.y = a.y + dy;
        bounds.radius = (float)Math.sqrt(dx*dx+dy*dy);
    }

    public boolean containsPoint(EcoPoint p) {
        if (!bounds.contains(p)) {
            return false;
        }
        final double x = ShapeUtil.dot(a, p, a, b);
        if (0 <= x && x <= ShapeUtil.dot(a, b, a, b)) {
            final float z = ShapeUtil.dot(a, p, a, d);
            return 0 <= z && z <= ShapeUtil.dot(a, d, a, d);
        }
        return false;
    }

    public boolean intersects(EcoCircle circle) {
        if (!bounds.intersects(circle)) {
            return false;
        }
        return containsPoint(circle.center)
                || circle.intersectsLine(a, b)
                || circle.intersectsLine(b, c)
                || circle.intersectsLine(c, d)
                || circle.intersectsLine(d, a);
    }

    public boolean intersects(EcoRectangle rect) {
        if (!bounds.intersects(rect.bounds)) {
            return false;
        }
        if (containsAnyRectPoint(rect) || rect.containsAnyRectPoint(this)) {
            return true;
        }
        EcoPoint last = rect.points.get(rect.points.size()-1);
        for (EcoPoint point : rect.points) {
            if (intersectsLine(last, point)) {
                return true;
            }
            last = point;
        }
        return false;
    }

    private boolean containsAnyRectPoint(EcoRectangle rect) {
        return containsPoint(rect.a) || containsPoint(rect.b)
                || containsPoint(rect.c) || containsPoint(rect.d);
    }

    boolean intersectsLine(EcoPoint a, EcoPoint b) {
        EcoPoint last = points.get(points.size()-1);
        for (EcoPoint point : points) {
            if (ShapeUtil.linesIntersect(a, b, last, point)) {
                return true;
            }
            last = point;
        }
        return false;
    }

    public String toData() {
        return ShapeUtil.pointListDoData(points);
    }
}
