package info.lusito.mapeditor.editors.properties.converters;

import javafx.util.converter.IntegerStringConverter;

public class LimitedIntegerStringConverter extends IntegerStringConverter {
    
    private Integer min;
    private Integer max;

    public LimitedIntegerStringConverter(String min, String max) {
        setMinMax(min, max);
    }
    
    public final void setMinMax(String min, String max) {
        this.min = ConvertUtil.toInteger(min);
        this.max = ConvertUtil.toInteger(max);
    }

    @Override
    public Integer fromString(String value) {
        Integer val = super.fromString(value);
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
