package info.lusito.mapeditor.editors.animation.filewatcher;

import info.lusito.mapeditor.persistence.animation.EcoAnimation;
import info.lusito.mapeditor.service.filewatcher.FileWatcher;
import info.lusito.mapeditor.service.filewatcher.FileWatcherFactory;
import java.io.InputStream;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = FileWatcherFactory.class)
public class AnimationFileWatcherFactory implements FileWatcherFactory {

    private final FileWatcher.Loader loader = new AnimationLoader();

    @Override
    public String getExtension() {
        return "xad";
    }

    @Override
    public FileWatcher create(FileObject dir) {
        return new FileWatcher(dir, getExtension(), loader);
    }

    private static class AnimationLoader extends FileWatcher.AbstractLoader {

        @Override
        protected Object load(InputStream stream) throws Exception {
            return EcoAnimation.load(stream);
        }

    }
}
