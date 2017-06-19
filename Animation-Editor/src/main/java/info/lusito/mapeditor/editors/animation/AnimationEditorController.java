package info.lusito.mapeditor.editors.animation;

import info.lusito.mapeditor.common.AbstractController;
import info.lusito.mapeditor.common.CheckedFileDrop;
import info.lusito.mapeditor.persistence.animation.EcoAnimation;
import info.lusito.mapeditor.persistence.animation.EcoAnimationHelper;
import info.lusito.mapeditor.persistence.common.EcoImageDefinition;
import info.lusito.mapeditor.persistence.common.EcoSize;
import info.lusito.mapeditor.projecttype.GameProject;
import info.lusito.mapeditor.projecttype.GameProjectUtil;
import info.lusito.mapeditor.service.filewatcher.FileWatcher;
import info.lusito.mapeditor.service.filewatcher.WatchedFile;
import info.lusito.mapeditor.service.filewatcher.picker.FilePicker;
import info.lusito.mapeditor.utils.PropertyFactory;
import info.lusito.mapeditor.utils.UndoUtil;
import info.lusito.mapeditor.utils.undo.SimpleEnumProperty;
import java.awt.Canvas;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.openide.filesystems.FileObject;

public class AnimationEditorController extends AbstractController<AnimationDataObject> implements Initializable {

    @FXML
    private TextField image;
    @FXML
    TextField gridX;
    @FXML
    TextField gridY;
    @FXML
    TextField width;
    @FXML
    TextField height;
    @FXML
    private Label errorLabel;
    private CheckedFileDrop imageFileDrop;
    @FXML
    private TextField frames;
    @FXML
    private TextField durations;
    @FXML
    private ComboBox<EcoAnimation.Mode> mode;
    @FXML
    private ColorPicker bgColor;
    @FXML
    private ImageView imageView;
    private SpriteAnimation spriteAnimation;
    private EcoAnimation animation;
    private boolean loaded;
    private final SimpleEnumProperty<EcoAnimation.Mode> type = PropertyFactory.createEnum(EcoAnimation.Mode.NORMAL);
    private FileObject currentImageFO;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bgColor.setValue(Color.TRANSPARENT);
        bgColor.getCustomColors().add(Color.TRANSPARENT);
//        gridNoMatch.visibleProperty().bind(calculatedTilesetData.invalid);
//        gridNoMatch.managedProperty().bind(calculatedTilesetData.invalid);
        mode.setItems(FXCollections.observableArrayList(EcoAnimation.Mode.values()));
        type.bindBidirectional(mode.valueProperty());
        mode.setValue(EcoAnimation.Mode.NORMAL);

        image.textProperty().addListener(this::onImageChanged);
        gridX.textProperty().addListener(this::onValueChanged);
        gridY.textProperty().addListener(this::onValueChanged);
        frames.textProperty().addListener(this::onValueChanged);
        durations.textProperty().addListener(this::onValueChanged);
        mode.valueProperty().addListener(this::onValueChanged);
        imageFileDrop = new CheckedFileDrop(image, this::getProject, this::onDropImage, false);
        imageFileDrop.addExtensions("png", "jpg", "jpeg", "tga");
        
        
        spriteAnimation = new SpriteAnimation(imageView);
    }

    private void onImageChanged(ObservableValue<? extends String> o, String ov, String nv) {
        checkImageChanged();
    }

    private void onValueChanged(ObservableValue<? extends String> o, String ov, String nv) {
        updateAndPlayAnimation();
    }

    private void onValueChanged(ObservableValue<? extends EcoAnimation.Mode> o, EcoAnimation.Mode ov, EcoAnimation.Mode nv) {
        updateAndPlayAnimation();
    }

    public void onClose() {
        if(spriteAnimation != null) {
            spriteAnimation.stop();
        }
    }

    private void checkImageChanged() {
        FileObject projectDir = GameProjectUtil.getProjectDir(dataObject);
        FileObject imageFO = projectDir.getFileObject(image.getText());
        if(imageFO != currentImageFO) {
            setImage(imageFO);
        }
    }

    @Override
    protected void afterRedo() {
        super.afterRedo();
        checkImageChanged();
    }

    @Override
    protected void afterUndo() {
        super.afterUndo();
        checkImageChanged();
    }

    @Override
    protected void onLoaded() {
        UndoUtil.removeUndoRedo(
                image,
                gridX,
                gridY,
                frames,
                durations
        );

        addStringPropertyListener(
            image,
            gridX,
            gridY,
            frames,
            durations
        );

        type.addListener(enumPropertyListener);

        dataObject.setController(this);
    }

    @Override
    protected void load(InputStream stream) throws IOException {
        FileObject projectDir = GameProjectUtil.getProjectDir(dataObject);
        animation = EcoAnimation.load(stream);
        if (animation.grid != null) {
            gridX.setText("" + animation.grid.x);
            gridY.setText("" + animation.grid.y);
        }
        if(animation.clip != null) {
            mode.setValue(animation.clip.mode);
            durations.setText(animation.clip.durations);
            frames.setText(animation.clip.frames);
        }
        if (animation.image != null) {
            width.setText("" + animation.image.width);
            height.setText("" + animation.image.height);
            if (animation.image.src != null) {
                image.setText(animation.image.src);
            }
        }
        loaded = true;
        updateAndPlayAnimation();
    }
    
    private void addStringPropertyListener(TextField ...texts) {
        for (TextField text : texts) {
            text.textProperty().addListener(stringPropertyListener);
        }
    }

    private void setImage(FileObject fo) {
        this.currentImageFO = fo;
        if(fo != null) {
            Image img = new Image(fo.toURL().toString());
            imageView.setImage(img);
            imageView.setFitWidth(0);
            imageView.setFitHeight(0);
            width.setText("" + (int) img.getWidth());
            height.setText("" + (int) img.getHeight());
            updateAndPlayAnimation();
        } else {
            imageView.setImage(null);
            width.setText("");
            height.setText("");
        }
    }
    
    private void updateAndPlayAnimation() {
        if(loaded) {
            if(updateAnimation())
                spriteAnimation.play(animation);
            else
                spriteAnimation.stop();
        }
    }
    
    private boolean updateAnimation() {
        StringBuilder error = new StringBuilder();
        if(animation.grid == null)
            animation.grid = new EcoSize();
        try {
            animation.grid.x = Integer.parseInt(gridX.getText());
        } catch(NumberFormatException e) {
            error.append("Columns is not a number\n");
        }
        if(animation.grid.x <= 0) {
            error.append("Columns must be > 0\n");
            animation.grid.x = 1;
        }
        try {
            animation.grid.y = Integer.parseInt(gridY.getText());
        } catch(NumberFormatException e) {
            error.append("Rows is not a number\n");
        }
        if(animation.grid.y <= 0) {
            error.append("Rows must be > 0\n");
            animation.grid.y = 1;
        }
        
        if(animation.clip == null)
            animation.clip = new EcoAnimation.ClipDefinition();
        animation.clip.frames = frames.getText();
        animation.clip.durations = durations.getText();
        try {
            int indexLimit = animation.grid.x*animation.grid.y;
            int[] indexes = EcoAnimationHelper.parseIndexes(animation.clip.frames, indexLimit, true);
            if(indexes != null)
                indexLimit = indexes.length;
            try {
                EcoAnimationHelper.parseDurations(animation.clip.durations, indexLimit, true);
            } catch(NumberFormatException e) {
                error.append("Durations contains a value that is not a number\n");
            } catch(IllegalArgumentException e) {
                error.append("Durations must contain at least one value\n");
            }
        } catch(NumberFormatException e) {
            error.append("Frames contains a value that is not a number\n");
        }

        animation.clip.mode = mode.getValue();
        if(animation.clip.mode == null) {
            animation.clip.mode = EcoAnimation.Mode.NORMAL;
            error.append("No mode selected\n");
        }
        
        if(animation.image == null)
            animation.image = new EcoImageDefinition();
        animation.image.src = image.getText();
        final Image img = imageView.getImage();
        if(img != null) {
            animation.image.width = (int)img.getWidth();
            animation.image.height = (int)img.getHeight();
            if(animation.image.width % animation.grid.x != 0) {
                error.append("Image width is not dividable by columns\n");
            }
            if(animation.image.height % animation.grid.y != 0) {
                error.append("Image height is not dividable by rows\n");
            }
        } else {
            animation.image.width = 0;
            animation.image.height = 0;
            error.append("No image selected\n");
        }
        String msg = error.toString();
        if(!msg.isEmpty()) {
            errorLabel.setText("Error:\n" + msg);
            return false;
        } else {
            errorLabel.setText("");
            return true;
        }
    }

    private void onDropImage(String relativePath) {
        image.setText(relativePath);
    }

    public EcoSize getSizeDefinition(TextField x, TextField y, boolean nullEmpty) {
        final String xt = x.getText();
        final String yt = y.getText();
        if (nullEmpty && xt.isEmpty() && yt.isEmpty()) {
            return null;
        }
        EcoSize size = new EcoSize();
        size.x = Integer.parseInt(xt);
        size.y = Integer.parseInt(yt);
        return size;
    }

    @Override
    public void save() throws IOException {
        updateAnimation();
        FileObject fo = dataObject.getPrimaryFile();
        try (BufferedOutputStream out = new BufferedOutputStream(fo.getOutputStream())) {
            animation.save(out);
            markUnmodified();
        }
    }

    @FXML
    private void onChooseImage(ActionEvent event) {
        FilePicker fp = new FilePicker();
        GameProject project = (GameProject) getProject();
        FileWatcher fileWatcher = project.getFileWatcher("*");
        fp.setAllowedExtension("png", "jpg", "jpeg", "tga");
        List<WatchedFile> files = fp.show(fileWatcher.getFiles(), "Select image");
        if (files != null && !files.isEmpty()) {
            for (WatchedFile file : files) {
                image.setText(file.getPath());
                break;
            }
        }
    }

    void setCanvas(Canvas canvas) {
//        swingNode.setContent(canvas.getC);
    }
}
