package info.lusito.mapeditor.projecttype;

import info.lusito.mapeditor.service.filewatcher.FileWatcherManager;
import info.lusito.mapeditor.service.filewatcher.FileWatcher;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class GameProject implements Project {

    private final FileObject projectDir;
    private final ProjectState state;
    private Lookup lkp;
    private final FileWatcherManager fileWatchers;

    GameProject(FileObject dir, ProjectState state) {
        this.projectDir = dir;
        this.state = state;
        fileWatchers = new FileWatcherManager(dir);
        dir.addRecursiveListener(new FileListUpdater(dir));
    }

    @Override
    public FileObject getProjectDirectory() {
        return projectDir;
    }
    
    public FileWatcher getFileWatcher(String extension) {
        return fileWatchers.get(extension);
    }

    @Override
    public Lookup getLookup() {
        if (lkp == null) {
            lkp = Lookups.fixed(new Object[]{
                this,
                new GameProjectInfo(this),
                new GameProjectLogicalView(this),
                new GameCustomizerProvider(this)
            });
        }
        return lkp;
    }


}
