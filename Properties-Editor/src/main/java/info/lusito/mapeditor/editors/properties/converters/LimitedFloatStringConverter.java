package info.lusito.mapeditor.editors.properties.converters;

import javafx.util.converter.FloatStringConverter;

public class LimitedFloatStringConverter extends FloatStringConverter {
    
    private Float min;
    private Float max;

    public LimitedFloatStringConverter(String min, String max) {
        setMinMax(min, max);
    }
    
    public final void setMinMax(String min, String max) {
        this.min = ConvertUtil.toFloat(min);
        this.max = ConvertUtil.toFloat(max);
    }

    @Override
    public Float fromString(String value) {
        Float val = super.fromString(value);
        if (val != null) {
            if (min != null && val < min) {
                val = min;
            } else if (max != null && val > max) {
                val = max;
            }
        }
        return val;
    }
    
}
