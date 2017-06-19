package info.lusito.mapeditor.service.filewatcher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

public class FileWatcherManager {
    private final Map<String, FileWatcher> watchers = new ConcurrentHashMap();
    private final FileObject dir;
    
    public FileWatcherManager(FileObject dir) {
        this.dir = dir;
        for (FileWatcherFactory factory : Lookup.getDefault().lookupAll(FileWatcherFactory.class)) {
            FileWatcher watcher = factory.create(dir);
            String key = factory.getExtension().toLowerCase();
            watchers.put(key, watcher);
        }
    }
    
    public FileWatcher get(String extension) {
        String key = extension.toLowerCase();
        FileWatcher watcher = watchers.get(key);
        if(watcher == null) {
            if(extension.equals("*")) {
                watcher = new FileWatcher(dir, "*");
                watchers.put("*", watcher);
            } else {
                for (FileWatcherFactory factory : Lookup.getDefault().lookupAll(FileWatcherFactory.class)) {
                    if(key.equals(factory.getExtension())) {
                        watcher = factory.create(dir);
                        watchers.put(key, watcher);
                        break;
                    }
                }
            }
        }
        return watcher;
    }
}
