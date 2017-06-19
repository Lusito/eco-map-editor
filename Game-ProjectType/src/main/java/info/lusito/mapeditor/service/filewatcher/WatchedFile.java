package info.lusito.mapeditor.service.filewatcher;

import org.openide.filesystems.FileObject;

public interface WatchedFile {

    String getDisplayName();

    String getFilename();

    String getPath();

    String getAbsolutePath();

    FileObject getFileobject();
    
    Object getContent();
}
