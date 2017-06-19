package info.lusito.mapeditor.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

public abstract class AbstractViewer<DOT extends DataObject> extends CloneableTopComponent {

    private static final long serialVersionUID = 3756357667616699666L;

    protected DOT dataObject;
    protected JToolBar toolbar;

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    protected void initialize(DOT dataObject) {
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled( false );
        JPopupMenu.setDefaultLightWeightPopupEnabled( false );

        this.dataObject = dataObject;

        toolbar = createToolBar();
        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.NORTH);
        setupPanel();

        setFocusable(true);
        updateName();
    }

    protected abstract void setupPanel();

    protected void updateNameInEDT() {
        Mutex.EVENT.readAccess(this::updateName);  
    }

    protected void updateName() {
        if(dataObject != null && dataObject.isValid()) {
            FileObject fo = dataObject.getPrimaryFile();
            String name = dataObject.getNodeDelegate().getDisplayName();
            if(dataObject.isModified()) {
                setHtmlDisplayName("<html><b>" + name);
                setToolTipText(FileUtil.getFileDisplayName(fo) + " (modified)");
            } else {
                setHtmlDisplayName(name);
                setToolTipText(FileUtil.getFileDisplayName(fo));
            }
        }
    }

    protected JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        toolBar.setFloatable(false);
        final Border border = BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(127,157,185));
        toolBar.setBorder(border);
        return toolBar;
    }

    @Override
    public void open() {
        if (dataObject == null) {
            return;
        }
        super.open();
    }

    @Override
    public void writeExternal(ObjectOutput out)
            throws IOException {
        super.writeExternal(out);
        out.writeObject(dataObject);
    }

    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        super.readExternal(in);
        initialize((DOT) in.readObject());
    }

    @Override
    protected String preferredID() {
        return getClass().getName();
    }
}
