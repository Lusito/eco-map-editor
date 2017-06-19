package info.lusito.mapeditor.editors.tileset;

import info.lusito.mapeditor.common.EcoPropertyFX;
import info.lusito.mapeditor.common.EcoPropertyUtil;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

class EcoTileInfoFX {

    private int x;
    private int y;
    private final ObservableList<EcoPropertyFX> properties = FXCollections.observableArrayList();

    EcoTileInfoFX(int x, int y, Map<String, String> properties) {
        this.x = x;
        this.y = y;
        if(properties != null)
            EcoPropertyUtil.load(properties, this.properties);
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public ObservableList<EcoPropertyFX> getProperties() {
        return properties;
    }

    public void addListeners(ChangeListener<String> stringListener) {
        for (EcoPropertyFX property : properties) {
            property.addListeners(stringListener);
        }
    }

    public void removeListeners(ChangeListener<String> stringListener) {
        for (EcoPropertyFX property : properties) {
            property.removeListeners(stringListener);
        }
    }
}
