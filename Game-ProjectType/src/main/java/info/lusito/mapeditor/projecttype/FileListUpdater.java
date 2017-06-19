package info.lusito.mapeditor.projecttype;

import info.lusito.mapeditor.utils.Toast;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Timer;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

public class FileListUpdater implements FileChangeListener {

    private final Timer timer;
    private final FileObject projectDir;

    public FileListUpdater(FileObject projectDir) {
        this.projectDir = projectDir;
        timer = new Timer(2000, this::doWrite);
        timer.setRepeats(false);
        timer.start();
    }

    private void doWrite(ActionEvent e) {
        List<String> files = new ArrayList();
        addFolder(projectDir, files);
        File projectDirFile = FileUtil.toFile(projectDir);
        Path path = projectDirFile.toPath().resolve("filelist.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            Collections.sort(files);
            for (String file : files) {
                writer.write(file + "\n");
            }
        } catch(IOException ex) {
            Toast.showError("Error writing filelist", ex.getMessage(), null);
        }
    }

    private void addFolder(FileObject subDir, List<String> files) {
        for (FileObject fileObject : subDir.getChildren()) {
            if (fileObject.isFolder()) {
                addFolder(fileObject, files);
            } else {
                files.add(FileUtil.getRelativePath(projectDir, fileObject));
            }
        }
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        timer.restart();
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        timer.restart();
    }

    @Override
    public void fileChanged(FileEvent fe) {
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        timer.restart();
    }

    @Override
    public void fileRenamed(FileRenameEvent fre) {
        timer.restart();
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fae) {
    }
}
