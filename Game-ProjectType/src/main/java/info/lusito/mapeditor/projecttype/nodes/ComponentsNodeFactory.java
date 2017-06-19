package info.lusito.mapeditor.projecttype.nodes;

import info.lusito.mapeditor.projecttype.GameProject;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

@NodeFactory.Registration(projectType = "info-lusito-mapeditor-game-project", position = 10)
public class ComponentsNodeFactory implements NodeFactory {

    @Override
    public NodeList<?> createNodes(Project project) {
        GameProject p = project.getLookup().lookup(GameProject.class);
        assert p != null;
        return new ComponentsNodeList(p);
    }

    private class ComponentsNodeList implements NodeList<Node> {

        GameProject project;

        public ComponentsNodeList(GameProject project) {
            this.project = project;
        }

        @Override
        public List<Node> keys() {
            List<Node> result = new ArrayList<>();
            addSpecialFolder(result, "components", "Components", "Components for the game");
            addSpecialFolder(result, "entities", "Entities", "Entity Blueprints");
            addSpecialFolder(result, "tilesets", "Tilesets", "Tilesets");
            addSpecialFolder(result, "images", "Images", "Images");
            addSpecialFolder(result, "animations", "Animations", "Animation Definitions");
            addSpecialFolder(result, "maps", "Maps", "Maps/Levels");
            addSpecialFolder(result, "sounds", "Sounds", "Sound files");
            addSpecialFolder(result, "music", "Music", "Music files");
            return result;
        }

        private void addSpecialFolder(List<Node> result, String name, String label, String description) {
            FileObject folder = project.getProjectDirectory().getFileObject(name);
            if (folder != null) {
                try {
                    final Node nodeDelegate = DataObject.find(folder).getNodeDelegate();
                    nodeDelegate.setDisplayName(label);
                    nodeDelegate.setShortDescription(description);
                    result.add(nodeDelegate);
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        @Override
        public Node node(Node node) {
            return new FilterNode(node);
        }

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }

        @Override
        public void addChangeListener(ChangeListener cl) {
        }

        @Override
        public void removeChangeListener(ChangeListener cl) {
        }
    }
}
