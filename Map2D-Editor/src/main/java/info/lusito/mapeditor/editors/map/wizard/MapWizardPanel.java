package info.lusito.mapeditor.editors.map.wizard;

import info.lusito.mapeditor.common.wizard.AbstractValidatedWizardPanel;
import javax.swing.JTextField;
import org.openide.WizardDescriptor;

public class MapWizardPanel extends AbstractValidatedWizardPanel {

    private InnnerPanel component;

    @Override
    public InnnerPanel getComponent() {
        if (component == null) {
            component = new InnnerPanel();
        }
        return component;
    }
    
    public void restoreTextField(WizardDescriptor wiz, JTextField field, String key) {
        Object value = wiz.getProperty(key);
        if(value instanceof String) {
            field.setText((String)value);
        }
    }
    
    public void storeTextField(WizardDescriptor wiz, JTextField field, String key) {
        wiz.putProperty(key, field.getText());
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        restoreTextField(wiz, component.width, "width");
        restoreTextField(wiz, component.height, "height");
        restoreTextField(wiz, component.tileWidth, "tileWidth");
        restoreTextField(wiz, component.tileHeight, "tileHeight");
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        storeTextField(wiz, component.width, "width");
        storeTextField(wiz, component.height, "height");
        storeTextField(wiz, component.tileWidth, "tileWidth");
        storeTextField(wiz, component.tileHeight, "tileHeight");
    }

    @Override
    public boolean isValid() {
        return isValidPositiveInt(component.width)
                && isValidPositiveInt(component.height)
                && isValidPositiveInt(component.tileWidth)
                && isValidPositiveInt(component.tileHeight);
    }
    
    public boolean isValidPositiveInt(JTextField field) {
        final String text = field.getText();
        if(!text.isEmpty()) {
            try {
                int value = Integer.parseInt(text);
                return value > 0;
            } catch(NumberFormatException e) {
            }
        }
        return false;
    }

    public final class InnnerPanel extends WizardFormPanel {

        private static final long serialVersionUID = 7744271719615675700L;

        public final JTextField width;
        public final JTextField height;
        public final JTextField tileWidth;
        public final JTextField tileHeight;

        public InnnerPanel() {
            tileWidth = addLabelAndTextField("Tile Width (in Pixels):", "32");
            tileHeight = addLabelAndTextField("Tile Height (in Pixels):", "32");
            width = addLabelAndTextField("Width (in Tiles):", "20");
            height = addLabelAndTextField("Height (in Tiles):", "20");
        }

        @Override
        public String getName() {
            return "Sizes";
        }

        @Override
        protected void onChange() {
            fireChange();
        }
    }
}
