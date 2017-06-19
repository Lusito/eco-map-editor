package info.lusito.mapeditor.service.filewatcher;

import org.openide.filesystems.FileObject;

class WatchedFileImpl implements WatchedFile {
    
    FileObject file;
    String path;
    Object content;
    private final FileWatcher.Loader loader;

    public WatchedFileImpl(FileWatcher.Loader loader) {
        this.loader = loader;
    }

    @Override
    public String getDisplayName() {
        return file.getName(); // for now
    }

    @Override
    public String getFilename() {
        return file.getNameExt();
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getAbsolutePath() {
        return file.getPath();
    }

    @Override
    public FileObject getFileobject() {
        return file;
    }

    @Override
    public Object getContent() {
        if(content == null && loader != null)
            content = loader.load(this);
        return content;
    }
    
}
