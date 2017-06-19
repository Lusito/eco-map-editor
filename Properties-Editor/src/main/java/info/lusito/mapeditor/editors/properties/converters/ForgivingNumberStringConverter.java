
package info.lusito.mapeditor.editors.properties.converters;

import javafx.util.converter.NumberStringConverter;

public class ForgivingNumberStringConverter extends NumberStringConverter {

    @Override
    public Number fromString(String value) {
        try {
            return super.fromString(value);
        } catch(Exception e) {
            return 0;
        }
    }
    
}
