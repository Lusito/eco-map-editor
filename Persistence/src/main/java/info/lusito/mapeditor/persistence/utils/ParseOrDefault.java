package info.lusito.mapeditor.persistence.utils;

public class ParseOrDefault {

    public static double getDouble(String value, double defaultValue) {
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
            }
        }
        return defaultValue;
    }

    public static float getFloat(String value, float defaultValue) {
        if (value != null) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
            }
        }
        return defaultValue;
    }

    public static int getInt(String value, int defaultValue) {
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
            }
        }
        return defaultValue;
    }

    public static boolean getBoolean(String value, boolean defaultValue) {
        if (value != null) {
            if (value.equalsIgnoreCase("true")) {
                return true;
            }
            if (value.equalsIgnoreCase("false")) {
                return false;
            }
        }
        return defaultValue;
    }

    public static <T> T getEnum(String value, Class<T> clazz, T defaultValue) {
        if (value != null) {
            try {
                return (T) Enum.valueOf((Class<Enum>) clazz, value);
            } catch (IllegalArgumentException ex) {
                for (Enum e : ((Class<Enum>) clazz).getEnumConstants()) {
                    if (e.name().compareToIgnoreCase(value) == 0) {
                        return (T) e;
                    }
                }
            }
        }
        return defaultValue;
    }
}
