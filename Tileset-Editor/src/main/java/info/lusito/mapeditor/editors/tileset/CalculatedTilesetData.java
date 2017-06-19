package info.lusito.mapeditor.editors.tileset;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;

public class CalculatedTilesetData {
    
    public int marginX;
    public int marginY;
    public int gridX;
    public int gridY;
    public int paddingX;
    public int paddingY;
    public int stepX;
    public int stepY;
    public int tilesX;
    public int tilesY;
    public final SimpleBooleanProperty invalid = new SimpleBooleanProperty(false);
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public void update(TilesetEditorController controller, Image image) {
        marginX = getInt(controller.marginX);
        marginY = getInt(controller.marginY);
        gridX = getInt(controller.gridX);
        gridY = getInt(controller.gridY);
        paddingX = getInt(controller.paddingX);
        paddingY = getInt(controller.paddingY);
        stepX = gridX + paddingX;
        stepY = gridY + paddingY;
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int widthNoMargin = width - 2 * marginX;
        int heightNoMargin = height - 2 * marginY;
        if(stepX <= 0 || stepY <= 0) {
            tilesX = 0;
            tilesY = 0;
            invalid.set(true);
        } else {
            tilesX = (widthNoMargin + paddingX) / stepX;
            tilesY = (heightNoMargin + paddingY) / stepY;
            final int widthNoMarginExpected = tilesX * stepX - paddingX;
            final int heightNoMarginExpected = tilesY * stepY - paddingY;
            invalid.set(widthNoMargin != widthNoMarginExpected || heightNoMargin != heightNoMarginExpected);
        }
        changeSupport.fireChange();
    }

    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    protected final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    private static int getInt(TextField textField) {
        try {
            String s = textField.textProperty().get();
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
}
