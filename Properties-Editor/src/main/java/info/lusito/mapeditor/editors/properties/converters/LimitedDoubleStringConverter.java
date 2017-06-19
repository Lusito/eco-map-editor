package info.lusito.mapeditor.editors.properties.converters;

import javafx.util.converter.DoubleStringConverter;

public class LimitedDoubleStringConverter extends DoubleStringConverter {
    
    private Double min;
    private Double max;

    public LimitedDoubleStringConverter(String min, String max){
        setMinMax(min, max);
    }
    
    public final void setMinMax(String min, String max) {
        this.min = ConvertUtil.toDouble(min);
        this.max = ConvertUtil.toDouble(max);
    }

    @Override
    public Double fromString(String value) {
        Double val = super.fromString(value);
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
