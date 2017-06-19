package info.lusito.mapeditor.persistence.map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.NamedMapConverter;
import java.util.HashMap;
import java.util.Map;

@XStreamAlias("component")
public class EcoMapEntityComponent {

    @XStreamAsAttribute
    public String type;
    
    @XStreamConverter(value = NamedMapConverter.class,
            strings = { "property", "key", "value" },
            booleans = { true, true },
            types = { String.class, String.class }, useImplicitType = false)
    public Map<String, String> properties;

    public EcoMapEntityComponent() {
        readResolve();
    }

    public EcoMapEntityComponent(String type) {
        this.type = type;
        readResolve();
    }

    public EcoMapEntityComponent(EcoMapEntityComponent other) {
        type = other.type;
        properties = new HashMap(other.properties);
    }

    private Object readResolve() {
        if(properties == null){
            properties = new HashMap();
        }
        return this;
    }
}
