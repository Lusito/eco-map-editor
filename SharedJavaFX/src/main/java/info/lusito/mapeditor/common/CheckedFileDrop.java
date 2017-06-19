package info.lusito.mapeditor.common;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class CheckedFileDrop {

    private final Supplier<Project> project;
    private final Consumer<String> consumer;
    private final boolean multiFile;
    private final HashSet<String> extensions = new HashSet();

    public CheckedFileDrop(Node node, Supplier<Project> project, Consumer<String> consumer, boolean multiFile) {
        this.project = project;
        this.consumer = consumer;
        this.multiFile = multiFile;
        node.setOnDragEntered(this::onDragEntered);
        node.setOnDragExited(this::onDragExited);
        node.setOnDragOver(this::onDragOver);
        node.setOnDragDropped(this::onDragDropped);
    }
    
    public void addExtensions(String... values) {
        for (String value : values) {
            extensions.add(value);
        }
    }

    private boolean isAcceptedExtension(File file) {
        if(extensions.isEmpty())
            return true;
        String ext = FileUtil.getExtension(file.getName()).toLowerCase();
        return extensions.contains(ext);
    }

    private void onDragEntered(DragEvent event) {
        event.consume();
    }

    private void onDragExited(DragEvent event) {
        event.consume();
    }

    private void onDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.LINK);
        }

        event.consume();
    }

    private void onDragDropped(DragEvent event) {
        final Dragboard dragboard = event.getDragboard();
        if (dragboard.hasFiles()) {
            event.acceptTransferModes(TransferMode.LINK);
            List<File> files = dragboard.getFiles();

            FileObject projectDirectory = project.get().getProjectDirectory();
            for (File file : files) {
                if (!isAcceptedExtension(file)) {
                    //fixme: inform user, that he can only add files of the specified extensions
                    continue;
                }
                String relativePath = FileUtil.getRelativePath(projectDirectory, FileUtil.toFileObject(file));
                if (relativePath != null) {
                    consumer.accept(relativePath);
                    if (!multiFile) {
                        break;
                    }
                } else {
                    //fixme: inform user, that he can not add files outside of the game directory
                }
            }
            event.setDropCompleted(true);
        }

        event.consume();
    }
}
