package info.lusito.mapeditor.projecttype;

import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.openide.util.ImageUtilities;

final class GameProjectInfo implements ProjectInformation {
    
    @StaticResource
    public static final String ICON = "info/lusito/mapeditor/projecttype/icon.png";
    private final GameProject project;

    public GameProjectInfo(GameProject project) {
        this.project = project;
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ImageUtilities.loadImage(ICON));
    }

    @Override
    public String getName() {
        return project.getProjectDirectory().getName();
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        /*do nothing, won't change*/
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        /*do nothing, won't change*/
    }

    @Override
    public Project getProject() {
        return project;
    }
    
}
