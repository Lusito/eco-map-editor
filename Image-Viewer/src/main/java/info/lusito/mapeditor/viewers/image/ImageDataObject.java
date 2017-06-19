package info.lusito.mapeditor.viewers.image;

import info.lusito.mapeditor.common.OpenableDataObject;
import java.io.IOException;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.CloneableTopComponent;

@Messages({
    "LBL_Image_LOADER=Files of Image"
})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_Image_LOADER",
        mimeType = "image/x-all-gdx",
        extension = {"png", "PNG", "jpg", "JPG", "jpeg", "JPEG", "tga", "TGA"}
)
@DataObject.Registration(
        mimeType = "image/x-all-gdx",
        iconBase = "info/lusito/mapeditor/viewers/image/icon.png",
        displayName = "#LBL_Image_LOADER",
        position = 300
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/image/x-all-gdx/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/image/x-all-gdx/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/image/x-all-gdx/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/image/x-all-gdx/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/image/x-all-gdx/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/image/x-all-gdx/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/image/x-all-gdx/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300
    ),
    @ActionReference(
            path = "Loaders/image/x-all-gdx/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400
    )
})
public class ImageDataObject extends OpenableDataObject<ImageDataObject> {

    private static final long serialVersionUID = 5126947516936082754L;

    public ImageDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
    }

    @Override
    public CloneableTopComponent createCloneableTopComponent(Entry entry) {
        return new ImageViewer((ImageDataObject) entry.getDataObject());
    }
}
