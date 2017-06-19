package info.lusito.mapeditor.editors.tileset.filewatcher;

import info.lusito.mapeditor.persistence.tileset.EcoTileset;
import info.lusito.mapeditor.service.filewatcher.FileWatcher;
import info.lusito.mapeditor.service.filewatcher.FileWatcherFactory;
import java.io.InputStream;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = FileWatcherFactory.class)
public class TilesetFileWatcherFactory implements FileWatcherFactory {
    private final FileWatcher.Loader loader = new TilesetLoader();

    @Override
    public String getExtension() {
        return "xtd";
    }

    @Override
    public FileWatcher create(FileObject dir) {
        return new FileWatcher(dir, getExtension(), loader);
    }
    
    private static class TilesetLoader extends FileWatcher.AbstractLoader {
        @Override
        protected Object load(InputStream stream) throws Exception {
            return EcoTileset.load(stream);
        }
        
    }
}
