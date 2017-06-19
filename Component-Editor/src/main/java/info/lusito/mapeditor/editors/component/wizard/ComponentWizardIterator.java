package info.lusito.mapeditor.editors.component.wizard;

import info.lusito.mapeditor.common.wizard.AbstractWizardIterator;
import info.lusito.mapeditor.persistence.component.EcoComponent;
import java.io.BufferedOutputStream;
import java.io.IOException;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

@TemplateRegistration(folder = "Map Editing", displayName = "#ComponentWizardIterator_displayName",
        iconBase = "info/lusito/mapeditor/editors/component/icon.png", description = "description.html",
        id = "component.xcd")
@Messages("ComponentWizardIterator_displayName=Component Definition")
public final class ComponentWizardIterator extends AbstractWizardIterator {

    @Override
    protected void populatePanels() {
        panels.add(createTargetChooser());
    }

    @Override
    protected void saveTo(FileObject fo, String targetName) throws IOException {
        EcoComponent cd = new EcoComponent();
        cd.name = targetName;

        try (BufferedOutputStream out = new BufferedOutputStream(fo.getOutputStream())) {
            cd.save(out);
        }
    }
}
