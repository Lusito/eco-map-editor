package info.lusito.mapeditor.persistence.component;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("property")
public class EcoComponentProperty {

    @XStreamAsAttribute
    public String name;

    @XStreamAsAttribute
    public String description;

    @XStreamAsAttribute
    public String type;

    @XStreamAsAttribute
    public Boolean multiple;

    @XStreamAsAttribute
    public String minimum;

    @XStreamAsAttribute
    public String maximum;

    @XStreamAsAttribute
    public String values;
}
