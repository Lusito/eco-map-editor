package info.lusito.mapeditor.common;

import java.io.IOException;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;

public abstract class SavableDataObject<ST, CT extends SaveCookie> extends OpenableDataObject<ST> {

    private static final long serialVersionUID = 5174403606332994160L;

    private CT controller;
    private Runnable modifiedListener;

    public SavableDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
    }

    public void setController(CT controller) {
        this.controller = controller;
    }

    public void setModifiedListener(Runnable runnable) {
        modifiedListener = runnable;
    }

    @Override
    public void setModified(boolean modified) {
        super.setModified(modified);
        if (controller != null) {
            if (modified) {
                getCookieSet().add(controller);
            } else {
                getCookieSet().remove(controller);
            }
        }
        if(modifiedListener != null)
            modifiedListener.run();
    }
}
