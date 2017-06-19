package info.lusito.mapeditor.common;

import java.awt.Component;
import java.awt.Graphics;
import java.io.IOException;
import javax.swing.Icon;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.util.ImageUtilities;

class CustomSavable extends AbstractSavable implements Icon {

    private final AbstractEditor tc;
    private final Icon icon;

    CustomSavable(AbstractEditor tc) {
        this.tc = tc;
        icon = ImageUtilities.image2Icon(tc.getIcon());
    }

    @Override
    protected String findDisplayName() {
        return tc.getDataObject().getPrimaryFile().getNameExt();
    }

    @Override
    protected void handleSave() throws IOException {
        tc.save();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CustomSavable) {
            CustomSavable m = (CustomSavable) obj;
            return tc == m.tc;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return tc.hashCode();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        icon.paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
        return icon.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return icon.getIconHeight();
    }

    void enable() {
        register();
    }

    void disable() {
        unregister();
    }
}
