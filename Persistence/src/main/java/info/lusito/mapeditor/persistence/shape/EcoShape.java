package info.lusito.mapeditor.persistence.shape;

public abstract class EcoShape {

    public abstract Type getType();
    
    public abstract EcoCircle getBounds();
    
    public abstract EcoShape copy();

    public enum Type {
        CIRCLE,
        RECTANGLE,
        POLYGON,
        POLYLINE
    }
}
