package info.lusito.mapeditor.editors.properties.nodes;

import info.lusito.mapeditor.common.CheckedFileDrop;
import info.lusito.mapeditor.editors.properties.PropertyTreeTableCell;
import info.lusito.mapeditor.editors.properties.api.PropertyInterface;
import info.lusito.mapeditor.projecttype.GameProject;
import info.lusito.mapeditor.service.filewatcher.FileWatcher;
import info.lusito.mapeditor.service.filewatcher.WatchedFile;
import info.lusito.mapeditor.service.filewatcher.picker.FilePicker;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class PropertyTreeNodeFile extends PropertyTreeNode {

    protected final HBox hbox = new HBox();
    protected final TextField textField = new TextField();
    protected final Button button = new Button("...");
    protected final GameProject project;
    private final CheckedFileDrop imageFileDrop;

    public PropertyTreeNodeFile(PropertyInterface property, GameProject project) {
        super(property);
        imageFileDrop = new CheckedFileDrop(textField, ()->project, this::onDropFile, false);
        hbox.setAlignment(Pos.CENTER);
        button.setPrefWidth(30);
        button.setMinWidth(30);
        button.setMaxWidth(30);
        hbox.getChildren().addAll(textField, button);
        HBox.setHgrow(textField, Priority.ALWAYS);
        button.setOnMouseClicked((e) -> showFileChooser());
        this.project = project;
        textField.textProperty().addListener((ov, o, n)-> applyValue(n));
    }

    private void onDropFile(String relativePath) {
        textField.setText(relativePath);
    }

    public void addToCell(PropertyTreeTableCell cell) {
        cell.setText(null);
        setValue(getValue(), false);
        cell.setControlGraphic(hbox);
        cell.addFocusSelect(textField, button);
    }

    private void showFileChooser() {
        boolean multipleValues = property.getMultiple();
        ArrayList<String> filenames = null;
        FilePicker fp = new FilePicker();
        FileWatcher fileWatcher = project.getFileWatcher("*");
        List<WatchedFile> files = fp.show(fileWatcher.getFiles(), "Select a file");
        if (files != null && !files.isEmpty()) {
            for (WatchedFile file : files) {
                if (!multipleValues) {
                    textField.setText(file.getPath());
                    return;
                } else {
                    if (filenames == null) {
                        filenames = new ArrayList();
                    }
                    filenames.add(file.getPath());
                }
            }
        }
        if(multipleValues && filenames != null) {
            textField.setText(String.join(";", filenames));
        }
    }

    @Override
    public void setValue(String value, boolean apply) {
        disableApplyValue = true;
        if (value == null) {
            textField.setText(getDefaultValue());
            textField.setDisable(true);
            button.setDisable(true);
        } else {
            textField.setText(value);
            textField.setDisable(false);
            button.setDisable(false);
        }
        disableApplyValue = false;

        super.setValue(value, apply);
    }

    @Override
    public void focusControl() {
        textField.requestFocus();
    }
}
