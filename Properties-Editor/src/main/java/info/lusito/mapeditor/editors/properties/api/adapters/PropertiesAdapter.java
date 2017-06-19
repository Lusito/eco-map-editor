package info.lusito.mapeditor.editors.properties.api.adapters;

import info.lusito.mapeditor.editors.properties.api.PropertiesGroupInterface;
import info.lusito.mapeditor.editors.properties.api.PropertiesInterface;
import info.lusito.mapeditor.projecttype.GameProject;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.openide.filesystems.FileObject;

public class PropertiesAdapter implements PropertiesInterface {

    protected final GameProject project;
    protected final Set<PropertiesInterface.Listener> listeners = ConcurrentHashMap.newKeySet();
    public final List<PropertiesGroupInterface> groups = new CopyOnWriteArrayList();
    public String title;
    public boolean loaded;
    public boolean instance;

    public PropertiesAdapter(GameProject project) {
        this.project = project;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }
    
    @Override
    public boolean isInstance() {
        return instance;
    }

    @Override
    public GameProject getProject() {
        return project;
    }

    @Override
    public FileObject getProjectDir() {
        return project.getProjectDirectory();
    }

    @Override
    public List<PropertiesGroupInterface> getGroups() {
        return groups;
    }

    @Override
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }


    public void updateEverything() {
        for (PropertiesInterface.Listener listener : listeners) {
            listener.onUpdateEverything();
        }
    }


    public void updateValues() {
        for (PropertiesInterface.Listener listener : listeners) {
            listener.onUpdateValues();
        }
    }

    public void close() {
        for (PropertiesInterface.Listener listener : listeners) {
            listener.onClose();
        }
    }
}
