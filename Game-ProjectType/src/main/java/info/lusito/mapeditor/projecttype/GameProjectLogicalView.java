package info.lusito.mapeditor.projecttype;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

class GameProjectLogicalView implements LogicalViewProvider {
    
    @StaticResource
    public static final String ICON = "info/lusito/mapeditor/projecttype/icon.png";
    private final GameProject project;

    public GameProjectLogicalView(GameProject project) {
        this.project = project;
    }

    @Override
    public Node createLogicalView() {
        try {
            //Obtain the project directory's node:
            FileObject projectDirectory = project.getProjectDirectory();
            DataFolder projectFolder = DataFolder.findFolder(projectDirectory);
            Node nodeOfProjectFolder = projectFolder.getNodeDelegate();
            //Decorate the project directory's node:
            return new ProjectNode(nodeOfProjectFolder, project);
        } catch (DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
            //Fallback-the directory couldn't be created
            //read-only filesystem or something evil happened
            return new AbstractNode(Children.LEAF);
        }
    }

    private final class ProjectNode extends FilterNode {

        final GameProject project;

        public ProjectNode(Node node, GameProject project) throws DataObjectNotFoundException {
            super(node, NodeFactorySupport.createCompositeChildren(project, "Projects/info-lusito-mapeditor-game-project/Nodes"), new ProxyLookup(new Lookup[]{Lookups.singleton(project), node.getLookup()}));
            this.project = project;
        }

        @Override
        public Action[] getActions(boolean arg0) {
            return new Action[]{CommonProjectActions.newFileAction(), CommonProjectActions.copyProjectAction(), CommonProjectActions.deleteProjectAction(), CommonProjectActions.closeProjectAction(), CommonProjectActions.customizeProjectAction()};
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage(ICON);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public String getDisplayName() {
            return project.getProjectDirectory().getName();
        }
    }

    @Override
    public Node findPath(Node root, Object target) {
        // leave unimplemented for now
        return null;
    }
    
}
