package info.lusito.mapeditor.editors.map.wizard;

import info.lusito.mapeditor.common.wizard.AbstractWizardIterator;
import info.lusito.mapeditor.persistence.common.EcoCompressionType;
import info.lusito.mapeditor.persistence.common.EcoSize;
import info.lusito.mapeditor.persistence.map.EcoMap;
import java.io.BufferedOutputStream;
import java.io.IOException;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

@TemplateRegistration(folder = "Map Editing", displayName = "#MapWizardIterator_displayName",
        iconBase = "info/lusito/mapeditor/editors/map/icon.png", description = "description.html",
        id = "map.xmd")
@Messages("MapWizardIterator_displayName=2D Map")
public final class MapWizardIterator extends AbstractWizardIterator {

    @Override
    protected void populatePanels() {
        panels.add(new MapWizardPanel());
        panels.add(createTargetChooser());
    }

    int getInt(String key) throws IOException {
        Object value = wizard.getProperty(key);
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                throw new IOException(key + " is not a number", e);
            }
        }
        throw new IOException("Value " + key + " not present");
    }

    @Override
    protected void saveTo(FileObject fo, String targetName) throws IOException {
        EcoMap md = new EcoMap();
        //fixme: compression type from input?
        md.compression = EcoCompressionType.NONE;
        md.name = targetName;
        md.size = new EcoSize(getInt("width"), getInt("height"));
        md.tileSize = new EcoSize(getInt("tileWidth"), getInt("tileHeight"));

        try (BufferedOutputStream out = new BufferedOutputStream(fo.getOutputStream())) {
            md.save(out);
        }
    }
}
