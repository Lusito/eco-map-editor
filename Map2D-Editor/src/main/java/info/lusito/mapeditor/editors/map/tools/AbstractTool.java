package info.lusito.mapeditor.editors.map.tools;

import com.badlogic.gdx.InputProcessor;
import info.lusito.mapeditor.editors.map.MapEditor;
import info.lusito.mapeditor.sharedlibgdx.camera.SimpleCamera;
import info.lusito.mapeditor.editors.map.model.EcoMapFX;
import info.lusito.mapeditor.persistence.map.EcoMap;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

public abstract class AbstractTool {
    
    protected final SimpleCamera camera;
    protected final EcoMapFX mapFX;
    protected final EcoMap map;
    protected final JToggleButton button;

    public AbstractTool(SimpleCamera camera, EcoMapFX mapFX, JToggleButton button) {
        this.camera = camera;
        this.mapFX = mapFX;
        this.map = mapFX.getMap();
        this.button = button;
    }
    
    public InputProcessor getInput() {
        return null;
    }
    
    public JToggleButton getButton() {
        return button;
    }
    
    protected static JToggleButton createButton(String imageName, String toolTipText, String altText) {
        String imgLocation = imageName + ".png";
        URL imageURL = AbstractTool.class.getResource(imgLocation);

        JToggleButton button = new JToggleButton();
        button.setToolTipText(toolTipText);

        if (imageURL != null) {
            button.setIcon(new ImageIcon(imageURL, altText));
        } else {
            button.setText(altText);
            System.err.println("Resource not found: " + imgLocation);
        }
        return button;
    }
    
    public void select() {
        button.setSelected(true);
    }
    
    public void deselect() {
        button.setSelected(false);
    }
    
    public void setButtonEnabled(boolean enabled) {
        button.setEnabled(enabled);
    }

    public boolean isSelected() {
        return button.isSelected();
    }

    public boolean isButtonEnabled() {
        return button.isEnabled();
    }
}
