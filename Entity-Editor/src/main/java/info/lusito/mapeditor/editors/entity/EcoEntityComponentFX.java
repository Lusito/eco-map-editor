package info.lusito.mapeditor.editors.entity;

import info.lusito.mapeditor.editors.properties.api.PropertiesGroupInterface;
import info.lusito.mapeditor.editors.properties.api.PropertyInterface;
import info.lusito.mapeditor.editors.properties.api.PropertyType;
import info.lusito.mapeditor.editors.properties.converters.ConvertUtil;
import info.lusito.mapeditor.persistence.component.EcoComponent;
import info.lusito.mapeditor.persistence.component.EcoComponentProperty;
import info.lusito.mapeditor.persistence.entity.EcoEntityComponent;
import info.lusito.mapeditor.utils.PropertyFactory;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;

public class EcoEntityComponentFX implements PropertiesGroupInterface {

    private final SimpleStringProperty name;
    private final SimpleStringProperty src;
    private final EcoComponent component;
    private final List<PropertyInterface> properties = new CopyOnWriteArrayList();
    private final MapProperty<String, String> propertiesMap = new SimpleMapProperty(FXCollections.observableHashMap());

    EcoEntityComponentFX(EcoEntityComponent ec, EcoComponent component, boolean silentCorrection) {
        this.name = PropertyFactory.createString(component.name);
        this.src = PropertyFactory.createString(ec.src);
        this.component = component;
        for (EcoComponentProperty property : component.properties) {
            String value = ec.properties.get(property.name);
            if(silentCorrection)
                value = correctValue(property, value);
            propertiesMap.put(property.name, value);
        }
        for (EcoComponentProperty property : component.properties) {
            properties.add(getAdapter(property));
        }
    }
    
    private String correctValue(EcoComponentProperty p, String value) {
        PropertyType type = PropertyType.getSafe(p.type);
        switch(type) {
            case FLOAT: {
                Float min = ConvertUtil.toFloat(p.minimum);
                Float max = ConvertUtil.toFloat(p.maximum);
                Float val = ConvertUtil.toFloat(value);
                if(val == null) {
                    val = 0f;
                }
                if (min != null && val < min) {
                    val = min;
                } else if (max != null && val > max) {
                    val = max;
                }
                return val.toString();
            }
            case INTEGER: {
                Integer min = ConvertUtil.toInteger(p.minimum);
                Integer max = ConvertUtil.toInteger(p.maximum);
                Integer val = ConvertUtil.toInteger(value);
                if(val == null) {
                    val = 0;
                }
                if (min != null && val < min) {
                    val = min;
                } else if (max != null && val > max) {
                    val = max;
                }
                return val.toString();
            }
            case SLIDER:{
                Double min = ConvertUtil.toDouble(p.minimum);
                Double max = ConvertUtil.toDouble(p.maximum);
                Double val = ConvertUtil.toDouble(value);
                if(val == null) {
                    val = 0.0;
                }
                if (min != null && val < min) {
                    val = min;
                } else if (max != null && val > max) {
                    val = max;
                }
                return val.toString();
            }
            case COLOR:
                return ConvertUtil.colorToString(ConvertUtil.toColor(value));
            case BOOLEAN:
                return value == null ? "false" : Boolean.valueOf(value).toString();
            case ENUM:
                if (p.values != null) {
                    final String[] split = p.values.split("\n");
                    for (String val : split) {
                        if (val.equals(value)) {
                            return value;
                        }
                    }
                    if (split.length > 0) {
                        return split[0];
                    }
                }
                break;
        }
        return value == null ? "" : value;
    }

    private EntityProperty getAdapter(EcoComponentProperty property) {
        EntityProperty adapter = new EntityProperty(this);

        adapter.name = property.name;
        adapter.description = property.description;
        adapter.type = PropertyType.getSafe(property.type);
        adapter.multiple = property.multiple == null ? false : property.multiple;
        adapter.minimum = property.minimum;
        adapter.maximum = property.maximum;
        if (property.values == null) {
            adapter.possibleValues = null;
        } else {
            adapter.possibleValues = new CopyOnWriteArrayList();
            for (String value : property.values.split("\n")) {
                adapter.possibleValues.add(value);
            }
        }

        adapter.value = propertiesMap.get(adapter.name);
        if (adapter.value == null) {
            adapter.value = "";
            propertiesMap.put(adapter.name, adapter.value);
        }
        adapter.defaultValue = null;
        return adapter;
    }

    void addListeners(ChangeListener<String> stringListener, MapChangeListener<String, String> mapListener) {
        name.addListener(stringListener);
        src.addListener(stringListener);
        propertiesMap.addListener(mapListener);
    }

    void removeListeners(ChangeListener<String> stringListener, MapChangeListener<String, String> mapListener) {
        name.removeListener(stringListener);
        src.removeListener(stringListener);
        propertiesMap.removeListener(mapListener);
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    @Override
    public String getName() {
        return name.get();
    }

    public SimpleStringProperty srcProperty() {
        return src;
    }

    public String getSrc() {
        return src.get();
    }

    @Override
    public String getDescription() {
        return component.description;
    }

    @Override
    public List<PropertyInterface> getProperties() {
        return properties;
    }

    void setPropertyValue(String key, String value) {
        if (value == null) {
            propertiesMap.remove(key);
        } else {
            propertiesMap.put(key, value);
        }
    }

    String getPropertyValue(String name) {
        return propertiesMap.get(name);
    }
}
