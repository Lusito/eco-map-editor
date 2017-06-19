package info.lusito.mapeditor.persistence.shape;

import info.lusito.mapeditor.persistence.utils.ParseOrDefault;

public class EcoCircle extends EcoShape {

    public final EcoPoint center = new EcoPoint();
    public float radius;

    public EcoCircle() {
    }

    public EcoCircle(EcoCircle other) {
        set(other);
    }

    public final void set(EcoCircle other) {
        center.x = other.center.x;
        center.y = other.center.y;
        radius = other.radius;
    }

    public EcoCircle(String data) {
        if(data != null) {
            String[] split = data.split(",");
            assert(split.length == 3);
            center.set(ParseOrDefault.getFloat(split[0], 0), ParseOrDefault.getFloat(split[1], 0));
            radius = ParseOrDefault.getFloat(split[2], 0);
        }
    }

    @Override
    public Type getType() {
        return Type.CIRCLE;
    }

    @Override
    public EcoCircle getBounds() {
        return this;
    }

    @Override
    public EcoShape copy() {
        return new EcoCircle(this);
    }

    public boolean contains(EcoPoint p) {
        return center.dist(p) < radius;
    }

    public boolean intersectsLine(EcoPoint a, EcoPoint b) {
        return ShapeUtil.pointLineDistance(a, b, center) < radius;
    }

    public boolean intersects(EcoCircle circle) {
        return center.dist(circle.center) < (radius + circle.radius);
    }

    public String toData() {
        return center.x + "," + center.y + "," + radius;
    }
}
