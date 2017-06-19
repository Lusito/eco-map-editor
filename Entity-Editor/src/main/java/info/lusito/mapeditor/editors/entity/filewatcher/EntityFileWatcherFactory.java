package info.lusito.mapeditor.editors.entity.filewatcher;

import info.lusito.mapeditor.persistence.entity.EcoEntity;
import info.lusito.mapeditor.service.filewatcher.FileWatcher;
import info.lusito.mapeditor.service.filewatcher.FileWatcherFactory;
import java.io.InputStream;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = FileWatcherFactory.class)
public class EntityFileWatcherFactory implements FileWatcherFactory {
    private final FileWatcher.Loader loader = new EntityLoader();

    @Override
    public String getExtension() {
        return "xed";
    }

    @Override
    public FileWatcher create(FileObject dir) {
        return new FileWatcher(dir, getExtension(), loader);
    }
    
    private static class EntityLoader extends FileWatcher.AbstractLoader {
        @Override
        protected Object load(InputStream stream) throws Exception {
            return EcoEntity.load(stream);
        }
    }
}
