package info.lusito.mapeditor.editors.properties.converters;

import javafx.scene.paint.Color;

public class ConvertUtil {

    public static Double toDouble(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return Double.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static Float toFloat(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return Float.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer toInteger(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return Integer.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static Color toColor(String value) {
        try {
            return Color.valueOf("#" + value);
        } catch(Exception e) {
            return Color.WHITE;
        }
    }

    public static Color toColor(String value, Color defaultValue) {
        try {
            return Color.valueOf("#" + value);
        } catch(Exception e) {
            return defaultValue;
        }
    }

    public static String colorToString(Color color) {
        int r = (int)Math.round(color.getRed() * 255.0);
        int g = (int)Math.round(color.getGreen() * 255.0);
        int b = (int)Math.round(color.getBlue() * 255.0);
        int o = (int)Math.round(color.getOpacity() * 255.0);
        return String.format("%02x%02x%02x%02x" , r, g, b, o);
    }
}
