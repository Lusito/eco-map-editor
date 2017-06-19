package info.lusito.mapeditor.persistence.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.NamedMapConverter;
import java.util.HashMap;
import java.util.Map;

@XStreamAlias("component")
public class EcoEntityComponent {

    @XStreamAsAttribute
    public String name;

    @XStreamAsAttribute
    public String src;
    
    @XStreamConverter(value = NamedMapConverter.class,
            strings = { "property", "key", "value" },
            booleans = { true, true },
            types = { String.class, String.class }, useImplicitType = false)
    public Map<String, String> properties;

    public EcoEntityComponent() {
        readResolve();
    }

    private Object readResolve() {
        if(properties == null){
            properties = new HashMap();
        }
        return this;
    }
}
