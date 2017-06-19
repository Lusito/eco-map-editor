package info.lusito.mapeditor.persistence.utils;

import info.lusito.mapeditor.persistence.map.EcoMap;
import info.lusito.mapeditor.persistence.map.EcoMapLayer;
import info.lusito.mapeditor.persistence.map.EcoMapLayerType;
import info.lusito.mapeditor.persistence.map.EcoTileLayer;
import info.lusito.mapeditor.persistence.tileset.EcoTileInfo;
import java.util.function.Consumer;

/**
 * Tries to combine certain tiles into larger rectangles.
 * This can be used to for example create physics objects.
 *
 * @author Santo Pfingsten
 */
public class EcoRectangleGenerator {

    public static interface RectangleGeneratorTest {

        boolean test(EcoTileLayer layer, EcoTileInfo info);
    }

    private final SimpleRect rectHorz = new SimpleRect();
    private final SimpleRect rectVert = new SimpleRect();
    private final SimpleRect rectBothX = new SimpleRect();
    private final SimpleRect rectBothY = new SimpleRect();

    public void generate(EcoMap map, RectangleGeneratorTest rectTest, Consumer<SimpleRect> consumer) {
        int width = map.size.x;
        int height = map.size.y;
        boolean[][] test = new boolean[width][height];
        for (EcoMapLayer layer : map.layers) {
            if (layer.getType() == EcoMapLayerType.TILE) {
                EcoTileLayer tileLayer = (EcoTileLayer)layer;
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        EcoTileInfo info = tileLayer.tiles[x][y];
                        if (info != null && !test[x][y] && rectTest.test(tileLayer, info)) {
                            test[x][y] = true;
                        }
                    }
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (test[x][y]) {
                    SimpleRect rect = getBestRect(x, y, test, width, height);
                    consumer.accept(rect);
                    cleartest(rect, test);
                }
            }
        }
    }

    private SimpleRect getBestRect(int x, int y, boolean[][] test, int width, int height) {
        updateRectHorz(x, y, test, width);
        updateRectVert(x, y, test, height);
        updateRectBoth(x, y, test, width, height);
        int sizeH = rectHorz.width * rectHorz.height;
        int sizeV = rectVert.width * rectVert.height;
        int sizeBX = rectBothX.width * rectBothX.height;
        int sizeBY = rectBothY.width * rectBothY.height;
        if (sizeH > sizeV) {
            if (sizeBX > sizeH) {
                return sizeBY > sizeBX ? rectBothY : rectBothX;
            }
            return sizeBY > sizeH ? rectBothY : rectHorz;
        } else {
            if (sizeBX > sizeV) {
                return sizeBY > sizeBX ? rectBothY : rectBothX;
            }
            return sizeBY > sizeV ? rectBothY : rectVert;
        }
    }

    private void initRect(SimpleRect rect, int x, int y) {
        rect.x = x;
        rect.y = y;
        rect.width = 1;
        rect.height = 1;
    }

    private void updateRectHorz(int x, int y, boolean[][] test, int width) {
        initRect(rectHorz, x, y);
        rectHorz.width = getHorzLength(x, y, test, width);
    }

    private int getHorzLength(int x, int y, boolean[][] test, int width) {
        int length = 1;
        for (x++; x < width && test[x][y]; x++) {
            length++;
        }
        return length;
    }

    private int getVertLength(int x, int y, boolean[][] test, int height) {
        int length = 1;
        for (y++; y < height && test[x][y]; y++) {
            length++;
        }
        return length;
    }

    private void updateRectVert(int x, int y, boolean[][] test, int height) {
        initRect(rectVert, x, y);
        rectVert.height = getVertLength(x, y, test, height);
    }

    private void updateRectBoth(int x, int y, boolean[][] test, int width, int height) {
        initRect(rectBothX, x, y);
        initRect(rectBothY, x, y);

        int maxWidth = rectHorz.width;
        int maxHeight = rectVert.height;

        rectBothY.height = maxHeight;
        for (int i = 1; i < maxWidth && getVertLength(x + i, y, test, height) == maxHeight; i++) {
            rectBothY.width++;
        }

        rectBothX.width = maxWidth;
        for (int i = 1; i < maxHeight && getHorzLength(x, y + i, test, width) == maxWidth; i++) {
            rectBothX.height++;
        }
    }

    private void cleartest(SimpleRect rect, boolean[][] test) {
        int endX = rect.x + rect.width;
        int endY = rect.y + rect.height;
        for (int x = rect.x; x < endX; x++) {
            for (int y = rect.y; y < endY; y++) {
                test[x][y] = false;
            }
        }
    }
}
