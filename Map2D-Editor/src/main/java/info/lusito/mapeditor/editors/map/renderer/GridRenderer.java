package info.lusito.mapeditor.editors.map.renderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import info.lusito.mapeditor.sharedlibgdx.SharedContextProvider;
import org.openide.util.Lookup;

public class GridRenderer {

    private final Color lineColor = new Color(0, 0, 0, 0.6f);
        //fixme: dispose
    private final Texture lineX;
    private final Texture lineY;

    public GridRenderer() {
        SharedContextProvider scp = Lookup.getDefault().lookup(SharedContextProvider.class);
        scp.pushContext();
        lineX = scp.getStippleTexture(4, 1, 2, 1);
        lineY = scp.getStippleTexture(1, 4, 1, 2);
        scp.popContext();
    }

    public void render(Batch batch, int tilesX, int tilesY, float gridX, float gridY, float zoomScale) {
        batch.setColor(lineColor);
        float width = tilesX * gridX / zoomScale;
        float height = tilesY * gridY / zoomScale;

        if((gridX / zoomScale) > 1 && (gridY / zoomScale) > 1) {
            // Show grid if it is not too dense
            for (int x = 0; x <= tilesX; x++) {
                drawLine(batch, lineY, x * gridX, 0, 1, height, zoomScale);
            }
            for (int y = 0; y <= tilesY; y++) {
                drawLine(batch, lineX, 0, y * gridY, width, 1, zoomScale);
            }
        } else {
            // Otherwise, just show the border
            drawLine(batch, lineY, gridX, 0, 1, height, zoomScale);
            drawLine(batch, lineY, tilesX * gridX, 0, 1, height, zoomScale);
            drawLine(batch, lineX, 0, gridY, width, 1, zoomScale);
            drawLine(batch, lineX, 0, tilesY * gridY, width, 1, zoomScale);
        }
    }

    private void drawLine(Batch batch, Texture texture, float x, float y, float width, float height, float zoomScale) {
        batch.draw(texture, x, y, width*zoomScale, height*zoomScale, 0, 0, (int) width,
                (int) height, false, true);
    }
}
