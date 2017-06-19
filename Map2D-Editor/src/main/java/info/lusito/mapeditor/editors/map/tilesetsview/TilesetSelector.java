package info.lusito.mapeditor.editors.map.tilesetsview;

import info.lusito.mapeditor.editors.map.model.TilesetInterface;
import info.lusito.mapeditor.persistence.tileset.EcoTileset;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class TilesetSelector {

    private final StackPane stackpane = new StackPane();
    private final Group scrollContent = new Group(stackpane);
    private final ImageView imageView;
    private final Canvas overlayCanvas;
    private Image image;
    private final TilesetsViewController controller;
    private final TilesetInterface tileset;
    private int selectedX = -1;
    private int selectedY = -1;
    private int drawWidth;
    private int drawHeight;
    private float zoom = 1;

    public TilesetSelector(ScrollPane scrollPane, TilesetsViewController controller, TilesetInterface tileset) {
        this.controller = controller;
        this.tileset = tileset;
        imageView = new ImageView();
        overlayCanvas = new Canvas();
        overlayCanvas.setOpacity(0.5);
        stackpane.getChildren().addAll(imageView, overlayCanvas);
        scrollPane.setContent(scrollContent);

        EcoTileset td = tileset.getTileset();
        int stepX = td.grid.x + td.padding.x;
        int stepY = td.grid.y + td.padding.y;
        
        int widthNoMargin = td.image.width - 2 * td.margin.x;
        int heightNoMargin = td.image.height - 2 * td.margin.y;
        int tilesX = (widthNoMargin + td.padding.x) / stepX;
        int tilesY = (heightNoMargin + td.padding.y) / stepY;
        scrollContent.setOnMouseClicked((e) -> {
            double x = e.getX()/zoom - td.margin.x;
            double y = e.getY()/zoom - td.margin.y;

            selectedX = Math.min(tilesX - 1, (int) (x / stepX));
            selectedY = Math.min(tilesY - 1, (int) (y / stepY));
            tileset.selectTile(selectedX, selectedY);
            updateOverlayImage();
        });

        int x = tileset.getSelectedTileX();
        int y = tileset.getSelectedTileY();
        if(x >= 0 && y >= 0) {
            selectedX = x;
            selectedY = y;
            updateOverlayImage();
        }
    }

    public Image setImage(String url) {
        image = new Image(url);
        imageView.setImage(image);
        updateZoom();
        return image;
    }
    
    private void updateZoom() {
        drawWidth = (int)(image.getWidth() * zoom);
        drawHeight = (int)(image.getHeight() * zoom);
        imageView.setFitWidth(drawWidth);
        imageView.setFitHeight(drawHeight);
        imageView.setX(0);
        imageView.setY(0);
        updateOverlayImage();
    }

    private void updateOverlayImage() {
        if (image != null) {
            GraphicsContext g = overlayCanvas.getGraphicsContext2D();
            overlayCanvas.setWidth(drawWidth);
            overlayCanvas.setHeight(drawHeight);
            g.clearRect(0, 0, drawWidth, drawHeight);
            
            if(selectedX >= 0 && selectedY >= 0) {
                EcoTileset td = tileset.getTileset();
                int x = td.margin.x + selectedX * (td.grid.x + td.padding.x);
                int y = td.margin.y + selectedY * (td.grid.y + td.padding.y);

                final Color color = Color.BLUE;
                g.setFill(color);

                g.fillRect(x*zoom, y*zoom, td.grid.x*zoom, td.grid.y*zoom);
            }
            overlayCanvas.setOpacity(0.5f);
        }
    }

    Image getImage() {
        return image;
    }

    void setZoom(float zoom) {
        this.zoom = zoom;
        updateZoom();
    }

}
