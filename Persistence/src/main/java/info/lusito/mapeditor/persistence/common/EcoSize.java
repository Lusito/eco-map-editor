package info.lusito.mapeditor.persistence.common;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class EcoSize {
    
    @XStreamAsAttribute
    public int x;
    @XStreamAsAttribute
    public int y;
    
    public EcoSize() {
    }
    
    public EcoSize(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
