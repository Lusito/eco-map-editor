package info.lusito.mapeditor.editors.map.layerview;

import info.lusito.mapeditor.common.dnd.DraggableListCell;
import info.lusito.mapeditor.common.dnd.DraggableRowSupport;
import info.lusito.mapeditor.editors.map.model.LayerInterface;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class LayerListCell extends DraggableListCell<LayerInterface> {

    private final static Image ICON_VISIBLE = new Image(LayerListCell.class.getResourceAsStream("visible.png"));
    private final static Image ICON_INVISIBLE = new Image(LayerListCell.class.getResourceAsStream("invisible.png"));
    private final static Image ICON_LOCKED = new Image(LayerListCell.class.getResourceAsStream("locked.png"));
    private final static Image ICON_UNLOCKED = new Image(LayerListCell.class.getResourceAsStream("unlocked.png"));
    private final static Image ICON_TILE = new Image(LayerListCell.class.getResourceAsStream("tile.png"));
    private final static Image ICON_ENTITY = new Image(LayerListCell.class.getResourceAsStream("entity.png"));
    private final static Image ICON_IMAGE = new Image(LayerListCell.class.getResourceAsStream("image.png"));

    public static Callback<ListView<LayerInterface>, ListCell<LayerInterface>> forListView(
            final Callback<LayerInterface, ObservableValue<Boolean>> getVisibleProperty,
            final Callback<LayerInterface, ObservableValue<Boolean>> getLockedProperty,
            DraggableRowSupport.MoveListener moveListener) {
        return list -> new LayerListCell(getVisibleProperty, getLockedProperty, list.hashCode(), moveListener);
    }

    private final HBox graphic = new HBox(5);
    private final ToggleImageView visible = new ToggleImageView(ICON_INVISIBLE, ICON_VISIBLE);
    private final ToggleImageView locked = new ToggleImageView(ICON_UNLOCKED, ICON_LOCKED);
    private final ImageView icon = new ImageView();
    private final Callback<LayerInterface, ObservableValue<Boolean>> getVisibleProperty;
    private final Callback<LayerInterface, ObservableValue<Boolean>> getLockedProperty;
    private ObservableValue<Boolean> visibleProperty;
    private ObservableValue<Boolean> lockedProperty;

    public LayerListCell(
            final Callback<LayerInterface, ObservableValue<Boolean>> getVisibleProperty,
            final Callback<LayerInterface, ObservableValue<Boolean>> getLockedProperty,
            int hashCode, DraggableRowSupport.MoveListener moveListener) {
        super(hashCode, moveListener);
        if (getVisibleProperty == null || getLockedProperty == null) {
            throw new NullPointerException(
                    "getVisibleProperty and getLockedProperty can not be null");
        }
        this.getVisibleProperty = getVisibleProperty;
        this.getLockedProperty = getLockedProperty;
        graphic.getChildren().addAll(visible, locked, icon);

        setAlignment(Pos.CENTER_LEFT);
        setContentDisplay(ContentDisplay.LEFT);

        // by default the graphic is null until the cell stops being empty
        setGraphic(null);
    }

    /** {@inheritDoc} */
    @Override
    public void updateItem(LayerInterface item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty) {
            visibleProperty = updateT(item, visible, visibleProperty, getVisibleProperty);
            lockedProperty = updateT(item, locked, lockedProperty, getLockedProperty);
            visible.setSelected(item.isVisible());
            locked.setSelected(item.isLocked());
            switch(item.getType()) {
                case TILE:
                    icon.setImage(ICON_TILE);
                    break;
                case ENTITY:
                    icon.setImage(ICON_ENTITY);
                    break;
                case IMAGE:
                    icon.setImage(ICON_IMAGE);
                    break;
            }
            setGraphic(graphic);
            setText(item.getName());
        } else {
            setGraphic(null);
            setText(null);
        }
    }

    private ObservableValue<Boolean> updateT(LayerInterface item, ToggleImageView checkBox,
            ObservableValue<Boolean> property,
            Callback<LayerInterface, ObservableValue<Boolean>> callback) {

        if (property != null) {
            checkBox.selectedProperty().unbindBidirectional((BooleanProperty)property);
        }
        property = callback.call(item);
        if (property != null) {
            checkBox.selectedProperty().bindBidirectional((BooleanProperty)property);
        }
        return property;
    }
}
