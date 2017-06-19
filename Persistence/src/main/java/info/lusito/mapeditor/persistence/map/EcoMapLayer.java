package info.lusito.mapeditor.persistence.map;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.converters.extended.NamedMapConverter;
import java.util.HashMap;
import java.util.Map;

public abstract class EcoMapLayer {

    @XStreamAsAttribute
    public String name;

    @XStreamAsAttribute
    public boolean locked;

    @XStreamAsAttribute
    public boolean visible = true;

    @XStreamAsAttribute
    public float opacity = 1;

    @XStreamConverter(value = NamedMapConverter.class,
            strings = { "property", "key", "value" },
            booleans = { true, true },
            types = { String.class, String.class }, useImplicitType = false)
    public Map<String, String> properties;

    @XStreamOmitField
    public Object attachment; // for user attachments

    public EcoMapLayer() {
    }

    public EcoMapLayer(EcoMapLayer layer) {
        name = layer.name;
        locked = layer.locked;
        visible = layer.visible;
        opacity = layer.opacity;

        properties = new HashMap(layer.properties);
    }

    public abstract EcoMapLayerType getType();
    
    public abstract void onAfterRead(EcoMap map);

    abstract void onBeforeWrite(EcoMap map);

    abstract void onAfterWrite();

    abstract EcoMapLayer createLightweightCopy();
}
