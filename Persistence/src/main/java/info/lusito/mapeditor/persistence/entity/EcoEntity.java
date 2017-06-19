package info.lusito.mapeditor.persistence.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.converters.extended.NamedMapConverter;
import info.lusito.mapeditor.persistence.utils.XStreamWrapped;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XStreamAlias("entity")
public class EcoEntity {

    @XStreamAsAttribute
    public String name;

    @XStreamAsAttribute
    public String description;

    @XStreamAsAttribute
    public String image;

    @XStreamAsAttribute
    @XStreamAlias("shape-color")
    public String shapeColor;

    @XStreamConverter(value = NamedMapConverter.class,
            strings = { "property", "key", "value" },
            booleans = { true, true },
            types = { String.class, String.class }, useImplicitType = false)
    public Map<String, String> properties;

    @XStreamImplicit
    public List<EcoEntityComponent> components;

    public EcoEntity() {
        readResolve();
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

    public void save(OutputStream stream) throws IOException {
        getXStream().toXML(this, stream);
    }

    public static EcoEntity load(InputStream stream) throws IOException {
        return (EcoEntity) getXStream().fromXML(stream);
    }
    
    private static XStreamWrapped<EcoEntity> getXStream() {
        return new XStreamWrapped().processAnnotations(EcoEntity.class, EcoEntityComponent.class);
    }
}
