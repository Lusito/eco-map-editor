package info.lusito.mapeditor.persistence.map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.converters.extended.NamedMapConverter;
import info.lusito.mapeditor.persistence.shape.EcoShape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XStreamAlias("entity")
public class EcoMapEntity {

    @XStreamAsAttribute
    public float x;

    @XStreamAsAttribute
    public float y;

    @XStreamAsAttribute
    public float scale = 1;

    @XStreamAsAttribute
    public float rotation;

    @XStreamAsAttribute
    public String type;

    @XStreamAsAttribute
    public String id;

	@XStreamConverter(ShapeConverter.class)
    public EcoShape shape;

    @XStreamConverter(value = NamedMapConverter.class,
            strings = { "property", "key", "value" },
            booleans = { true, true },
            types = { String.class, String.class }, useImplicitType = false)
    public Map<String, String> properties;

    @XStreamImplicit
    public List<EcoMapEntityComponent> components;

    @XStreamOmitField
    public Object attachment; // for user attachments

    public EcoMapEntity() {
        readResolve();
    }

    public EcoMapEntity(EcoMapEntity other) {
        this(other, false);
    }

    EcoMapEntity(EcoMapEntity other, boolean lightweight) {
        x = other.x;
        y = other.y;
        scale = other.scale;
        rotation = other.rotation;
        type = other.type;
        id = other.id;
        if (lightweight) {
            shape = other.shape;
        } else if (shape != null) {
            shape = shape.copy();
        }

        if (lightweight) {
            if(!other.properties.isEmpty())
                properties = new HashMap(other.properties);
        } else {
            properties = new HashMap(other.properties);
        }
        components = new ArrayList();
        if(lightweight) {
            for (EcoMapEntityComponent component : other.components) {
                if (!component.properties.isEmpty()) {
                    components.add(component);
                }
            }
            if (components.isEmpty()) {
                components = null;
            }
        } else {
            for (EcoMapEntityComponent component : other.components) {
                components.add(new EcoMapEntityComponent(component));
            }
        }
    }

    private Object readResolve() {
        if(properties == null){
            properties = new HashMap();
        }
        if(components == null){
            components = new ArrayList();
        }
        return this;
    }
    
    public EcoMapEntityComponent findComponent(String type, boolean create) {
        for (EcoMapEntityComponent component : components) {
            if(type.equals(component.type))
                return component;
        }
        if(create) {
            EcoMapEntityComponent component = new EcoMapEntityComponent(type);
            components.add(component);
            return component;
        }
        return null;
    }
}
