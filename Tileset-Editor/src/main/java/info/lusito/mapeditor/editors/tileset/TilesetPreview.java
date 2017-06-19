package info.lusito.mapeditor.editors.tileset;

import java.nio.IntBuffer;
import java.util.Set;
import javafx.event.Event;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javax.swing.event.ChangeEvent;

public class TilesetPreview {

    private final static int ZOOM_PIXEL_SIZE = 8;
    private static final int ZOOM_VIEW_SIZE = 16;
    private static final int ZOOM_VIEW_SIZE_HALF = 8;
    private static final int ZOOM_SIDE = ZOOM_PIXEL_SIZE * ZOOM_VIEW_SIZE;
    private final int[] pixels = new int[ZOOM_VIEW_SIZE * ZOOM_VIEW_SIZE];
    private final int[] newPixels = new int[ZOOM_SIDE * ZOOM_SIDE];
    private final WritablePixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();
    private static final Color selectColor = new Color(0, 0, 1, 0.5);

    private final StackPane stackpane = new StackPane();
    private final Group scrollContent = new Group(stackpane);
    private final ImageView imageView;
    private final Canvas overlayCanvas;
    private final WritableImage zoomImage = new WritableImage(128, 128);
    private final SnapshotParameters param = new SnapshotParameters();
    private Image image;
    private final TilesetEditorController controller;
    private final CalculatedTilesetData calculated;
    private int lastMouseX;
    private int lastMouseY;
    private int selectedX = -1;
    private int selectedY = -1;
    private boolean propertyMode = true;

    public TilesetPreview(ScrollPane scrollPane, TilesetEditorController controller) {
        this.controller = controller;
        calculated = controller.calculatedTilesetData;
        imageView = new ImageView();
        overlayCanvas = new Canvas();
        overlayCanvas.setOpacity(0.5);
        stackpane.getChildren().addAll(imageView, overlayCanvas);
        scrollPane.setContent(scrollContent);

        scrollContent.setOnMouseMoved(this::onMouseMove);
        scrollContent.setOnMouseDragged(this::onMouseDragged);
        scrollContent.setOnMouseClicked(this::onMouseClicked);
        calculated.addChangeListener(this::onCalculatedChanged);
        controller.gridColor.setOnAction(this::onGridAction);
    }
    
    public void onMouseMove(MouseEvent e) {
        lastMouseX = (int) e.getX();
        lastMouseY = (int) e.getY();
        updateZoomCanvas(lastMouseX, lastMouseY);
    }
    
    public void onMouseDragged(MouseEvent e) {
        if(!propertyMode) {
            onMouseClicked(e);
        }
    }
    
    public void onMouseClicked(MouseEvent e) {
        final boolean leftMouse = e.getButton() == MouseButton.PRIMARY;
        
        double x = e.getX() - calculated.marginX;
        double y = e.getY() - calculated.marginY;

        int tileX = Math.min(calculated.tilesX - 1, (int) (x / calculated.stepX));
        int tileY = Math.min(calculated.tilesY - 1, (int) (y / calculated.stepY));

        if(propertyMode) {
            if(leftMouse) {
                controller.onTileClicked(tileX, tileY);
            } else {
                controller.onDeselect();
            }
        } else {
            float tilePosX = tileX * calculated.stepX;
            float tilePosY = tileY * calculated.stepY;
            float inTileX = (float)x - tilePosX;
            float inTileY = (float)y - tilePosY;
            int tx = tileX * 2 + (inTileX >= calculated.gridX/2 ? 1 : 0);
            int ty = tileY * 2 + (inTileY >= calculated.gridY/2 ? 1 : 0);
            int id = tx | (ty << 16);
            if(leftMouse) {
                controller.addQuarter(id);
            } else {
                controller.removeQuarter(id);
            }
            updateOverlayImage();
        }
    }

    private void onGridAction(Event e) {
        updateOverlayImage();
    }

    private void onCalculatedChanged(ChangeEvent e) {
        updateOverlayImage();
    }
    
    public void select(int tileX, int tileY) {
        selectedX = tileX;
        selectedY = tileY;
        updateOverlayImage();
    }
    
    public void deselect() {
        selectedX = -1;
        selectedY = -1;
        updateOverlayImage();
    }
    
    public void clearImage() {
        image = null;
        imageView.setImage(null);
    }

    public Image setImage(String url) {
        image = new Image(url);
        imageView.setImage(image);
        calculated.update(controller, image);
        updateOverlayImage();
        return image;
    }

    public void updateOverlayImage() {
        if(image == null)
            return;

        GraphicsContext g = overlayCanvas.getGraphicsContext2D();
        final double width = image.getWidth();
        final double height = image.getHeight();
        overlayCanvas.setWidth(width);
        overlayCanvas.setHeight(height);
        g.clearRect(0, 0, width, height);

        final Color gridColor = controller.gridColor.valueProperty().get();
        g.setFill(new Color(gridColor.getRed(), gridColor.getGreen(), gridColor.getBlue(), 1));
        double opacity = gridColor.getOpacity();
        if(!propertyMode)
            opacity *= 0.5;
        overlayCanvas.setOpacity(opacity);
        
        if(calculated.gridX <= 0 || calculated.gridY <= 0) {
            updateZoomCanvas(lastMouseX, lastMouseY);
            return;
        }
        final int stepX = calculated.gridX + calculated.paddingX;
        final int stepY = calculated.gridY + calculated.paddingY;
        if(!propertyMode) {
            final int halfGridX = calculated.gridX/2;
            final int halfGridY = calculated.gridY/2;
            Set<Integer> quarters = controller.getQuarters();
            if(quarters != null) {
                for (Integer quarter : quarters) {
                    int qx = quarter & 0xFFFF;
                    int qy = (quarter & 0xFFFF0000) >> 16;
                    int tileX = qx / 2;
                    int tileY = qy / 2;
                    int x = calculated.marginX + stepX * tileX;
                    int y = calculated.marginX + stepY * tileY;
                    if(qx % 2 == 1)
                        x += halfGridX;
                    if(qy % 2 == 1)
                        y += halfGridY;
                    g.fillRect(x, y, halfGridX, halfGridY);
                }
            }
            updateZoomCanvas(lastMouseX, lastMouseY);
        } else {
            if (calculated.marginX > 0) {
                g.fillRect(0, 0, calculated.marginX, height);
                g.fillRect(width - calculated.marginX, 0, calculated.marginX, height);
            }
            if (calculated.marginY > 0) {
                g.fillRect(0, 0, width, calculated.marginY);
                g.fillRect(0, height - calculated.marginY, width, calculated.marginY);
            }
            if (calculated.paddingX > 0) {
                for (int x = calculated.marginX + calculated.gridX; x < width; x += stepX) {
                    g.fillRect(x, 0, calculated.paddingX, height);
                }
            } else {
                // dashed line
                g.setLineWidth(1);
                g.setStroke(gridColor);
                g.setLineDashes(4, 6);
                g.beginPath();
                for (int x = calculated.marginX + calculated.gridX; x < width; x += stepX) {
                    g.moveTo(x, 0);
                    g.lineTo(x, height);
                }
                g.closePath();
                g.stroke();
            }
            if (calculated.paddingY > 0) {
                for (int y = calculated.marginY + calculated.gridY; y < height; y += stepY) {
                    g.fillRect(0, y, width, calculated.paddingY);
                }
            } else {
                // dashed line
                g.setLineWidth(1);
                g.setStroke(gridColor);
                g.setLineDashes(4, 6);
                g.beginPath();
                for (int y = calculated.marginY + calculated.gridY; y < height; y += stepY) {
                    g.moveTo(0, y);
                    g.lineTo(width, y);
                }
                g.closePath();
                g.stroke();
            }

            if (selectedX >= 0 && selectedY >= 0) {
                int x = calculated.marginX + selectedX * stepX;
                int y = calculated.marginY + selectedY * stepY;

                g.setFill(selectColor);

                g.fillRect(x, y, calculated.gridX, calculated.gridY);
            }

            updateZoomCanvas(lastMouseX, lastMouseY);
        }
    }

    private void updateZoomCanvas(int mx, int my) {
        if (image != null) {
            int maxX = (int) image.getWidth() - ZOOM_VIEW_SIZE;
            int maxY = (int) image.getHeight() - ZOOM_VIEW_SIZE;
            int vpx = Math.min(maxX, Math.max(0, mx - ZOOM_VIEW_SIZE_HALF));
            int vpy = Math.min(maxY, Math.max(0, my - ZOOM_VIEW_SIZE_HALF));

            Rectangle2D viewport = new Rectangle2D(vpx, vpy, ZOOM_VIEW_SIZE, ZOOM_VIEW_SIZE);
            param.setViewport(viewport);
            scrollContent.snapshot(param, zoomImage);
            GraphicsContext gc = controller.zoomCanvas.getGraphicsContext2D();
            PixelWriter pw = gc.getPixelWriter();
            PixelReader pr = zoomImage.getPixelReader();
            pr.getPixels(0, 0, ZOOM_VIEW_SIZE, ZOOM_VIEW_SIZE, pixelFormat, pixels, 0, ZOOM_VIEW_SIZE);

            int op = 0, np = 0;
            while (op < pixels.length) {
                for (int y = 0; y < ZOOM_PIXEL_SIZE; y++) {
                    for (int po = 0; po < ZOOM_VIEW_SIZE; po++) {
                        final int pixel = pixels[op + po];
                        for (int x = 0; x < ZOOM_PIXEL_SIZE; x++) {
                            newPixels[np++] = pixel;
                        }
                    }
                }
                op += ZOOM_VIEW_SIZE;
            }

            pw.setPixels(0, 0, ZOOM_SIDE, ZOOM_SIDE, pixelFormat, newPixels, 0, ZOOM_SIDE);
        }
    }

    Image getImage() {
        return image;
    }

    void setPropertyMode(boolean newValue) {
        propertyMode = newValue;
        updateOverlayImage();
    }
}
