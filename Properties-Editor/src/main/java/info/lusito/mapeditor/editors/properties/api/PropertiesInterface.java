package info.lusito.mapeditor.editors.properties.api;

import info.lusito.mapeditor.projecttype.GameProject;
import java.util.List;
import org.openide.filesystems.FileObject;

public interface PropertiesInterface {

    String getTitle();

    boolean isLoaded();

    boolean isInstance();

//    String getName();
    GameProject getProject();

    FileObject getProjectDir();

    List<PropertiesGroupInterface> getGroups();

    void addListener(Listener listener);

    void removeListener(Listener listener);

    public static interface Listener {

        void onUpdateEverything();

        void onUpdateValues();

        void onClose();
    }
}
