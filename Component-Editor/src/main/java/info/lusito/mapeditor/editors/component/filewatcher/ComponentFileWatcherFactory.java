package info.lusito.mapeditor.editors.component.filewatcher;

import info.lusito.mapeditor.persistence.component.EcoComponent;
import info.lusito.mapeditor.service.filewatcher.FileWatcher;
import info.lusito.mapeditor.service.filewatcher.FileWatcherFactory;
import java.io.InputStream;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = FileWatcherFactory.class)
public class ComponentFileWatcherFactory implements FileWatcherFactory {
    private final FileWatcher.Loader loader = new ComponentLoader();

    @Override
    public String getExtension() {
        return "xcd";
    }

    @Override
    public FileWatcher create(FileObject dir) {
        return new FileWatcher(dir, getExtension(), loader);
    }
    
    private static class ComponentLoader extends FileWatcher.AbstractLoader {
        @Override
        protected Object load(InputStream stream) throws Exception {
            return EcoComponent.load(stream);
        }
    }
}
