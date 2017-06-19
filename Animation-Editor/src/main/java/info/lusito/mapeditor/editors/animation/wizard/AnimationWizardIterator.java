package info.lusito.mapeditor.editors.animation.wizard;

import info.lusito.mapeditor.common.wizard.AbstractWizardIterator;
import info.lusito.mapeditor.persistence.animation.EcoAnimation;
import java.io.BufferedOutputStream;
import java.io.IOException;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

@TemplateRegistration(folder = "Asset Editing", displayName = "#AnimationWizardIterator_displayName",
        iconBase = "info/lusito/mapeditor/editors/animation/icon.png", description = "description.html",
        id = "animation.xad")
@Messages("AnimationWizardIterator_displayName=Animation Definition")
public final class AnimationWizardIterator extends AbstractWizardIterator {

    @Override
    protected void populatePanels() {
        panels.add(createTargetChooser());
    }

    @Override
    protected void saveTo(FileObject fo, String targetName) throws IOException {
        EcoAnimation ad = new EcoAnimation();
//        ad.name = targetName;

        try (BufferedOutputStream out = new BufferedOutputStream(fo.getOutputStream())) {
            ad.save(out);
        }
    }
}
