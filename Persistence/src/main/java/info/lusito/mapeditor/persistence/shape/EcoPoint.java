package info.lusito.mapeditor.persistence.shape;

public class EcoPoint {

    public float x;
    public float y;

    public EcoPoint() {
    }

    public EcoPoint(EcoPoint other) {
        this.x = other.x;
        this.y = other.y;
    }

    public EcoPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(float angle) {
        final float degreesToRadians = (float) (Math.PI / 180);
        float radians = angle * degreesToRadians;
        set((float) Math.cos(radians), (float) Math.sin(radians));
    }

    public float dist(EcoPoint p) {
        float dx = p.x - x;
        float dy = p.y - y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
