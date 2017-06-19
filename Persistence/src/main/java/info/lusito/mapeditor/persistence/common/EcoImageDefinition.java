package info.lusito.mapeditor.persistence.common;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class EcoImageDefinition {
    
    @XStreamAsAttribute
    public String src;
    @XStreamAsAttribute
    public int width;
    @XStreamAsAttribute
    public int height;
    
}
