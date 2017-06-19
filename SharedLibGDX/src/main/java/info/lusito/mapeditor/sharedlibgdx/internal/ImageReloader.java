package info.lusito.mapeditor.sharedlibgdx.internal;

import info.lusito.mapeditor.service.filewatcher.FileWatcher;
import info.lusito.mapeditor.service.filewatcher.WatchedFile;
import info.lusito.mapeditor.sharedlibgdx.SharedContextProvider;

public class ImageReloader implements FileWatcher.Listener {

    private final SharedContextProvider scp;

    public ImageReloader(SharedContextProvider scp) {
        this.scp = scp;
    }

    @Override
    public void onFileChanged(WatchedFile file) {
        scp.getCanvas().postRunnable(() -> {
            String ext = file.getFileobject().getExt().toLowerCase();
            if (ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("tga")) {
                scp.reloadTexture(file.getAbsolutePath());
            }
        });
    }

    @Override
    public void onFileCreated(WatchedFile file) {
    }

    @Override
    public void onFileDeleted(WatchedFile file) {
    }

}
