package info.lusito.mapeditor.editors.tileset.wizard;

import info.lusito.mapeditor.common.wizard.AbstractWizardIterator;
import info.lusito.mapeditor.persistence.tileset.EcoTileset;
import java.io.BufferedOutputStream;
import java.io.IOException;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

@TemplateRegistration(folder = "Map Editing", displayName = "#TilesetWizardIterator_displayName",
        iconBase = "info/lusito/mapeditor/editors/tileset/icon.png", description = "description.html",
        id = "tileset.xtd")
@Messages("TilesetWizardIterator_displayName=Tileset Definition")
public final class TilesetWizardIterator extends AbstractWizardIterator {

    @Override
    protected void populatePanels() {
        panels.add(createTargetChooser());
    }

    @Override
    protected void saveTo(FileObject fo, String targetName) throws IOException {
        EcoTileset td = new EcoTileset();
        td.name = targetName;

        try (BufferedOutputStream out = new BufferedOutputStream(fo.getOutputStream())) {
            td.save(out);
        }
    }
}
