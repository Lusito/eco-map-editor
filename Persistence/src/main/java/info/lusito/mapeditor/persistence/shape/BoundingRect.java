package info.lusito.mapeditor.persistence.shape;

public class BoundingRect {

    protected final EcoPoint topLeft = new EcoPoint();
    protected final EcoPoint bottomRight = new EcoPoint();
    protected final EcoPoint size = new EcoPoint();
    protected final EcoPoint center = new EcoPoint();
    protected final EcoPoint halfSize = new EcoPoint();
    protected final EcoRectangle rect = new EcoRectangle();

    public float getX() {
        return topLeft.x;
    }

    public float getY() {
        return topLeft.y;
    }

    public float getWidth() {
        return size.x;
    }

    public float getHeight() {
        return size.y;
    }

    public float getRight() {
        return bottomRight.x;
    }

    public float getBottom() {
        return bottomRight.y;
    }

    public void set(float x, float y, float width, float height) {
        topLeft.set(x, y);
        size.set(width, height);
        halfSize.set(width / 2, height / 2);
        bottomRight.set(x + width, y + height);
        center.set(x + halfSize.x, y + halfSize.y);
        rect.set(center.x, center.y, width, height, 0);
    }

    public boolean contains(EcoPoint point) {
        return contains(point.x, point.y);
    }

    public boolean contains(float x, float y) {
        return x >= x && y >= y && x < bottomRight.x && y < bottomRight.y;
    }

    public boolean intersects(BoundingRect rect) {
        return Math.abs(center.x - rect.center.x) < halfSize.x + rect.halfSize.x
                && Math.abs(center.y - rect.center.y) < halfSize.y + rect.halfSize.y;
    }

    public boolean intersects(EcoCircle circle) {
        float dx = Math.max(Math.abs(circle.center.x - center.x) - halfSize.x, 0);
        float dy = Math.max(Math.abs(circle.center.y - center.y) - halfSize.y, 0);
        return (dx * dx + dy * dy) < (circle.radius * circle.radius);
    }

    public boolean intersects(EcoRectangle rect) {
        return rect.intersects(this.rect);
    }
}
