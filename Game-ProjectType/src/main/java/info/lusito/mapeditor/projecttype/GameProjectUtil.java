package info.lusito.mapeditor.projecttype;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

public class GameProjectUtil {

    public static String getRelativePath(DataObject fromObj, FileObject file) {
        return FileUtil.getRelativePath(getProjectDir(fromObj), file);
    }

    public static FileObject getProjectDir(DataObject obj) {
        Project project = FileOwnerQuery.getOwner(obj.getPrimaryFile());
        return project.getProjectDirectory();
    }

    public static InputStream getInputStream(DataObject fromObj, String filename) throws FileNotFoundException {
        return getInputStream(getProjectDir(fromObj), filename);
    }
    
    public static InputStream getInputStream(FileObject projectDirObj, String filename) throws FileNotFoundException {
        File projectDir = FileUtil.toFile(projectDirObj);
        File file = new File(projectDir, filename);
        return new BufferedInputStream(new FileInputStream(file));
    }
}
