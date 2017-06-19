package info.lusito.mapeditor.service.filewatcher;

import info.lusito.mapeditor.utils.DialogUtil;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public class FileWatcher implements FileChangeListener {

    private final Set<FileWatcher.Listener> listeners = ConcurrentHashMap.newKeySet();
    private final Map<String, WatchedFileImpl> files = new ConcurrentHashMap();
    private final FileObject dir;
    private final String extension;
    private final Loader loader;

    public FileWatcher(FileObject dir, String extension) {
        this(dir, extension, null);
    }
    
    public FileWatcher(FileObject dir, String extension, Loader loader) {
        this.dir = dir;
        this.extension = extension;
        this.loader = loader;
        addFolder(dir);
        dir.addRecursiveListener(this);
    }
    
    private void addFolder(FileObject subDir) {
        for (FileObject fileObject : subDir.getChildren()) {
            if(fileObject.isFolder())
                addFolder(fileObject);
            else if(isValidExt(fileObject))
                addFile(fileObject);
        }
    }

    private void addFile(FileObject fileObject) {
        WatchedFileImpl file = new WatchedFileImpl(loader);
        file.path = FileUtil.getRelativePath(dir, fileObject);
        file.file = fileObject;
        files.put(file.path, file);
    }
    
    private boolean isValidExt(FileObject fileObject) {
        if(extension.equalsIgnoreCase("*"))
            return true;
        return extension.equalsIgnoreCase(fileObject.getExt());
    }

    public String getExtension() {
        return extension;
    }
    
    public Collection<WatchedFile> getFiles() {
        synchronized(files) {
            return new ArrayList(files.values());
        }
    }
    
    public WatchedFile getFile(String path) {
        synchronized(files) {
            return files.get(path);
        }
    }

    public synchronized void addListener(Listener listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(FileWatcher.Listener listener) {
        listeners.remove(listener);
    }

    private synchronized void fireFileChanged(WatchedFile file) {
        for (Listener listener : listeners) {
            listener.onFileChanged(file);
        }
    }

    private synchronized void fireFileCreated(WatchedFile file) {
        for (Listener listener : listeners) {
            listener.onFileCreated(file);
        }
    }

    private synchronized void fireFileDeleted(WatchedFile file) {
        for (Listener listener : listeners) {
            listener.onFileDeleted(file);
        }
    }

    private void createFile(FileObject fo, String path) {
        WatchedFileImpl file = new WatchedFileImpl(loader);
        file.path = path;
        file.file = fo;
        synchronized(files) {
            files.put(path, file);
        }
        fireFileCreated(file);
    }

    private void updateFile(WatchedFileImpl file) {
        file.content = null;
        fireFileChanged(file);
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        // Don't care
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        final FileObject fo = fe.getFile();
        if(!isValidExt(fo))
            return;
        String path = FileUtil.getRelativePath(dir, fo);
        WatchedFileImpl file = (WatchedFileImpl) getFile(path);
        if(file != null) {
            //should not happen, but just in case, treat it like a file updated.
            updateFile(file);
        } else {
            createFile(fo, path);
        }
    }

    @Override
    public void fileChanged(FileEvent fe) {
        final FileObject fo = fe.getFile();
        if(!isValidExt(fo))
            return;
        String path = FileUtil.getRelativePath(dir, fo);
        WatchedFileImpl file = files.get(path);
        if(file == null) {
            //should not happen, but just in case, treat it like a file created.
            createFile(fo, path);
        } else {
            updateFile(file);
        }
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        final FileObject fo = fe.getFile();
        if(!isValidExt(fo))
            return;
        String path = FileUtil.getRelativePath(dir, fo);
        WatchedFileImpl file = (WatchedFileImpl) getFile(path);
        if(file != null) {
            fireFileDeleted(file);
        }
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        FileObject newFO = fe.getFile();
        if(!isValidExt(newFO))
            return;
        String oldPath = FileUtil.getRelativePath(dir, newFO.getParent());
        oldPath += '/' + fe.getName() + '.' + fe.getExt();
        
        synchronized(files) {
            WatchedFileImpl file = files.remove(oldPath);
            if(file != null) {
                file.path = FileUtil.getRelativePath(dir, newFO);
                file.file = newFO;
                files.put(file.path, file);
                //fixme: notify?
            }
        }
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        // Don't care
    }

    public interface Listener {

        public void onFileChanged(WatchedFile file);

        public void onFileCreated(WatchedFile file);

        public void onFileDeleted(WatchedFile file);
    }

    public interface Loader {

        public Object load(WatchedFile file);
    }
    
    public static abstract class AbstractLoader implements Loader {
        
        @Override
        public Object load(WatchedFile file) {
            FileObject fo = file.getFileobject();
            try (InputStream stream = new BufferedInputStream(fo.getInputStream())) {
                return load(stream);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                DialogUtil.showException("Error loading file: " + file.getPath(), ex);
            }
            return null;
        }

        protected abstract Object load(InputStream stream) throws Exception;
    }
}
