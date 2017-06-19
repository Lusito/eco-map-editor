package info.lusito.mapeditor.common;

import java.io.IOException;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.OpenSupport;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.windows.CloneableTopComponent;

public abstract class OpenableDataObject<ST> extends MultiDataObject implements CookieSet.Factory {

    private static final long serialVersionUID = -714492068776903115L;

    private transient CustomOpenSupport openSupport;

    public OpenableDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        getCookieSet().add(CustomOpenSupport.class, this);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    public Node.Cookie createCookie(Class clazz) {
        if (clazz.isAssignableFrom(CustomOpenSupport.class)) {
            return getOpenSupport();
        } else {
            return null;
        }
    }

    private synchronized CustomOpenSupport getOpenSupport() {
        if (openSupport == null) {
            openSupport = new CustomOpenSupport(getPrimaryEntry());
        }
        return openSupport;
    }

    public abstract CloneableTopComponent createCloneableTopComponent(MultiDataObject.Entry entry);

    private class CustomOpenSupport extends OpenSupport implements OpenCookie, CloseCookie {

        public CustomOpenSupport(MultiDataObject.Entry entry) {
            super(entry, new OpenSupport.Env(entry.getDataObject()));
        }

        public CloneableTopComponent createCloneableTopComponent() {
            return OpenableDataObject.this.createCloneableTopComponent(entry);
        }
    }
}
