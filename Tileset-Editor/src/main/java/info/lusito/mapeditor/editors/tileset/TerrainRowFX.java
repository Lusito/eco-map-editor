package info.lusito.mapeditor.editors.tileset;

import info.lusito.mapeditor.utils.PropertyFactory;
import java.util.HashSet;
import java.util.Set;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;

public class TerrainRowFX {

    private final SimpleStringProperty name;
    private final Set<Integer> quarterIds;

    public TerrainRowFX(String name) {
        this(name, new HashSet());
    }

    public TerrainRowFX(String name, Set<Integer> quarterIds) {
        this.name = PropertyFactory.createString(name);
        this.quarterIds = quarterIds;
    }
    
    public void addListeners(ChangeListener<String> stringListener) {
        name.addListener(stringListener);
    }

    public void removeListeners(ChangeListener<String> stringListener) {
        name.removeListener(stringListener);
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String value) {
        name.set(value);
    }
    
    public void addQuarter(int id) {
        quarterIds.add(id);
    }
    
    public void removeQuarter(int id) {
        quarterIds.remove(id);
    }

    Set<Integer> getQuarters() {
        return quarterIds;
    }

    boolean hasQuarter(int id) {
        return quarterIds.contains(id);
    }
}
