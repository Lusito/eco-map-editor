package info.lusito.mapeditor.persistence.tileset;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.converters.extended.NamedMapConverter;
import info.lusito.mapeditor.persistence.utils.ParseOrDefault;
import java.util.Map;

@XStreamAlias("tile")
public class EcoTileInfo {

    @XStreamOmitField
    public EcoTileset tileset;

    @XStreamAsAttribute
    public int x;

    @XStreamAsAttribute
    public int y;

    @XStreamConverter(value = NamedMapConverter.class,
            strings = { "property", "key", "value" },
            booleans = { true, true },
            types = { String.class, String.class }, useImplicitType = false)
    public Map<String, String> properties;
    
    @XStreamOmitField
    public int textureX;
    
    @XStreamOmitField
    public int textureY;
    
    @XStreamOmitField
    public int terrainBits;

    //fixme: adapt for animations
    public void updateCoords() {
        textureX = x * tileset.grid.x;
        textureX += tileset.margin.x + x * tileset.padding.x;
        textureY = y * tileset.grid.y;
        textureY += tileset.margin.y + y * tileset.padding.y;
    }

    public String getProperty(String propertyName, String def) {
        if (properties == null) {
            return def;
        }
        return properties.getOrDefault(propertyName, def);
    }

    public int getIntProperty(String propertyName, int def) {
        if (properties == null) {
            return def;
        }
        return ParseOrDefault.getInt(properties.get(propertyName), def);
    }

    public float getFloatProperty(String propertyName, float def) {
        if (properties == null) {
            return def;
        }
        return ParseOrDefault.getFloat(properties.get(propertyName), def);
    }

    public double getDoubleProperty(String propertyName, double def) {
        if (properties == null) {
            return def;
        }
        return ParseOrDefault.getDouble(properties.get(propertyName), def);
    }

    public boolean getBooleanProperty(String propertyName, boolean def) {
        if (properties == null) {
            return def;
        }
        return ParseOrDefault.getBoolean(properties.get(propertyName), def);
    }

    public <T> T getEnumProperty(String propertyName, Class<T> clazz, T def) {
        if (properties == null) {
            return def;
        }
        return ParseOrDefault.getEnum(properties.get(propertyName), clazz, def);
    }
}
