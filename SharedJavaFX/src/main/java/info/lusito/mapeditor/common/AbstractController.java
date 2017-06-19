package info.lusito.mapeditor.common;

import info.lusito.mapeditor.utils.DialogUtil;
import info.lusito.mapeditor.utils.undo.UndoContext;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.awt.UndoRedo;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject;
import org.openide.util.Exceptions;

public abstract class AbstractController<T extends MultiDataObject> extends UndoContext implements SaveCookie {

    // For undo/redo support
    protected UndoRedo.Manager manager;
    protected T dataObject;

    public final void setDataObject(T obj, UndoRedo.Manager mgr) {
        manager = mgr;
        dataObject = obj;

        // Load from file
        FileObject fo = dataObject.getPrimaryFile();
        try (InputStream stream = new BufferedInputStream(fo.getInputStream())) {
            load(stream);
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
            DialogUtil.showException("Error: File not found: " + fo.getPath(), ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            DialogUtil.showException("Error loading file: " + fo.getPath(), ex);
        }

        onLoaded();
    }

    protected abstract void onLoaded();

    protected abstract void load(final InputStream stream) throws IOException;

    @Override
    public final void addUndoableEdit(UndoableEdit edit) {
        onUndoableEditAdded();
        manager.undoableEditHappened(new UndoableEditEvent(this, edit));
    }

    @Override
    public final void setModified(boolean modified) {
        dataObject.setModified(modified);
    }

    public Project getProject() {
        return FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
    }
}
