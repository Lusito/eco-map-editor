package info.lusito.mapeditor.editors.entity.wizard;

import info.lusito.mapeditor.common.wizard.AbstractWizardIterator;
import info.lusito.mapeditor.persistence.entity.EcoEntity;
import java.io.BufferedOutputStream;
import java.io.IOException;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

@TemplateRegistration(folder = "Map Editing", displayName = "#EntityWizardIterator_displayName",
        iconBase = "info/lusito/mapeditor/editors/entity/icon.png", description = "description.html",
        id = "entity.xed")
@Messages("EntityWizardIterator_displayName=Entity Blueprint")
public final class EntityWizardIterator extends AbstractWizardIterator {

    @Override
    protected void populatePanels() {
        panels.add(createTargetChooser());
    }

    @Override
    protected void saveTo(FileObject fo, String targetName) throws IOException {
        EcoEntity ed = new EcoEntity();
        ed.name = targetName;

        try (BufferedOutputStream out = new BufferedOutputStream(fo.getOutputStream())) {
            ed.save(out);
        }
    }
}
