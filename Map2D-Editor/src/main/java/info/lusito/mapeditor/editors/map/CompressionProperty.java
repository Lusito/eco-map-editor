package info.lusito.mapeditor.editors.map;

import info.lusito.mapeditor.editors.properties.api.PropertyType;
import info.lusito.mapeditor.editors.properties.api.adapters.PropertyAdapter;
import info.lusito.mapeditor.persistence.common.EcoCompressionType;
import info.lusito.mapeditor.persistence.map.EcoMap;
import info.lusito.mapeditor.persistence.utils.ParseOrDefault;
import java.util.ArrayList;

public class CompressionProperty extends PropertyAdapter {

    public EcoMap map;

    public CompressionProperty() {
        name = "Compression";
        description = "Compression type";
        type = PropertyType.ENUM;
        possibleValues = new ArrayList();
        for (EcoCompressionType type : EcoCompressionType.values()) {
            possibleValues.add(type.name());
        }
    }

    @Override
    public void setValue(String value) {
        map.compression = ParseOrDefault.getEnum(value, EcoCompressionType.class, EcoCompressionType.NONE);
    }

    @Override
    public String getValue() {
        return map.compression.name();
    }
}
