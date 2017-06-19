package info.lusito.mapeditor.editors.map.filedrop;

import org.openide.filesystems.FileObject;

public interface FileDropListener {

    boolean onFileDrop(FileObject fileObject, String relativePath, float x, float y);
}
