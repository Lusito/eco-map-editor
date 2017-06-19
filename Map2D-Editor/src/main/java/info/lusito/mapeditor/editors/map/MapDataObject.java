package info.lusito.mapeditor.editors.map;

import info.lusito.mapeditor.common.SavableDataObject;
import java.io.IOException;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.CloneableTopComponent;

@Messages({
    "LBL_Map_LOADER=Files of Map"
})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_Map_LOADER",
        mimeType = "text/ecomap-map+xml",
        extension = {"xmd"}
)
@DataObject.Registration(
        mimeType = "text/ecomap-map+xml",
        iconBase = "info/lusito/mapeditor/editors/map/icon.png",
        displayName = "#LBL_Map_LOADER",
        position = 300
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/ecomap-map+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/text/ecomap-map+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/ecomap-map+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/text/ecomap-map+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/text/ecomap-map+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/text/ecomap-map+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/text/ecomap-map+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300
    ),
    @ActionReference(
            path = "Loaders/text/ecomap-map+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400
    )
})
public class MapDataObject extends SavableDataObject<MapDataObject, MapEditorController> {

    private static final long serialVersionUID = -2491748265436426209L;

    public MapDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
    }

    @Override
    public CloneableTopComponent createCloneableTopComponent(MultiDataObject.Entry entry) {
        return new MapEditor((MapDataObject) entry.getDataObject());
    }
}
