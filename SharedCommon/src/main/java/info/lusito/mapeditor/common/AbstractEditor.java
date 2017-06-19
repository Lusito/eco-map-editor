package info.lusito.mapeditor.common;

import info.lusito.mapeditor.utils.DialogUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.netbeans.api.actions.Savable;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.MultiDataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

public abstract class AbstractEditor<DOT extends MultiDataObject, CT extends Savable>
        extends AbstractViewer<DOT> {

    private static final long serialVersionUID = 5009257636011163487L;

    protected UndoRedo.Manager manager = new UndoRedo.Manager();
    protected CT controller;
    protected final InstanceContent content = new InstanceContent();
    protected final Lookup lookup;
    private final CustomSavable mySavable;

    public AbstractEditor() {
        Lookup dynamicLookup = new AbstractLookup(content);
        lookup = new ProxyLookup(dynamicLookup, super.getLookup());
        mySavable = new CustomSavable(this);
    }

    protected DOT getDataObject() {
        return dataObject;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public UndoRedo getUndoRedo() {
        return manager;
    }

    @Override
    protected void initialize(DOT dataObject) {
        if (dataObject instanceof SavableDataObject) {
            ((SavableDataObject) dataObject).setModifiedListener(this::onModifiedChange);
        }
        super.initialize(dataObject);
    }

    @Override
    public boolean canClose() {
        if (dataObject.isModified()) {
            String filename = dataObject.getNodeDelegate().getDisplayName();
            DialogUtil.Result result = DialogUtil.confirmSaveDiscardCancel("Question", "File " + filename + " is modified. Save?");
            if (result == DialogUtil.Result.CANCEL) {
                return false;
            }
            if (result == DialogUtil.Result.DISCARD) {
                discard();
            } else if (result == DialogUtil.Result.SAVE) {
                try {
                    save();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    DialogUtil.showException("Error: File could not be saved: " + dataObject.getName(), ex);
                    return false;
                }
            }
            return true;
        }
        return super.canClose();
    }

    protected void save() throws IOException {
        if (controller != null) {
            Path source = FileUtil.toFile(dataObject.getPrimaryFile()).toPath();
            Path backup = source.resolveSibling(source.getFileName() + ".bak");
            try {
                Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                DialogUtil.message("Failed to create backup.. are you missing write access?", DialogUtil.Icon.ERROR);
                e.printStackTrace();
                return;
            }
            try {
                controller.save();
            } catch(Throwable t) {
                // copy backup back
                try {
                    Files.copy(backup, source, StandardCopyOption.REPLACE_EXISTING);
                    backup.toFile().delete();
                } catch (IOException e) {
                    DialogUtil.message("Failed to write backup back.. backup is still on disc tho.", DialogUtil.Icon.ERROR);
                    e.printStackTrace();
                }
                throw t;
            }
            backup.toFile().delete();
            dataObject.setModified(false);
        }
    }

    protected void discard() {
        dataObject.setModified(false);
    }

    protected void onModifiedChange() {
        setModified(dataObject.isModified());
        updateNameInEDT();
    }

    private void setModified(boolean modified) {
        if (modified) {
            if (getLookup().lookup(CustomSavable.class) == null) {
                mySavable.enable();
                content.add(mySavable);
            }
        } else {
            content.remove(mySavable);
            mySavable.disable();
        }
    }
}
