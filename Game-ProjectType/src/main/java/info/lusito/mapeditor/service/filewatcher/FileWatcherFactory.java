package info.lusito.mapeditor.service.filewatcher;

import org.openide.filesystems.FileObject;

public interface FileWatcherFactory {
    
    String getExtension();
    
    FileWatcher create(FileObject dir);
}
