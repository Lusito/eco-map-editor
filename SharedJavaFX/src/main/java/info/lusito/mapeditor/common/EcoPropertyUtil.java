package info.lusito.mapeditor.common;

import java.util.HashMap;
import java.util.Map;
import javafx.collections.ObservableList;

public class EcoPropertyUtil {

    public static Map<String, String> save(ObservableList<EcoPropertyFX> from) {
        if (from.isEmpty()) {
            return null;
        }
        HashMap<String, String> out = new HashMap();
        for (EcoPropertyFX property : from) {
            out.put(property.getKey(), property.getValue());
        }
        return out;
    }

    public static void load(Map<String, String> from, ObservableList<EcoPropertyFX> to) {
        if (from != null) {
            for (Map.Entry<String, String> entry : from.entrySet()) {
                to.add(new EcoPropertyFX(entry.getKey(), entry.getValue()));
            }
        }
    }
}
