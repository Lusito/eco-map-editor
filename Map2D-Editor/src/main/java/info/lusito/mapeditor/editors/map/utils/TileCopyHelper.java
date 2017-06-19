package info.lusito.mapeditor.editors.map.utils;

import info.lusito.mapeditor.persistence.tileset.EcoTileInfo;

public class TileCopyHelper {
    
    private final int srcX;
    private final int srcY;
    private final int destX;
    private final int destY;
    private final int width;
    private final int height;

    public TileCopyHelper(int srcWidth, int srcHeight, int destWidth, int destHeight, int destX, int destY) {
        if (destX >= 0) {
            this.destX = destX;
            srcX = 0;
        } else {
            this.destX = 0;
            srcX = -destX;
        }
        if (destY >= 0) {
            this.destY = destY;
            srcY = 0;
        } else {
            this.destY = 0;
            srcY = -destY;
        }
        int srcX2 = srcWidth;
        if (destX + srcWidth > destWidth) {
            srcX2 -= (destX + srcWidth - destWidth);
        }
        int srcY2 = srcHeight;
        if (destY + srcHeight > destHeight) {
            srcY2 -= (destY + srcHeight - destHeight);
        }
        width = srcX2 - srcX;
        height = srcY2 - srcY;
    }

    public void copyTiles(EcoTileInfo[][] src, EcoTileInfo[][] dest) {
        for (int x = 0; x < width; x++) {
            System.arraycopy(src[srcX + x], srcY, dest[destX + x], destY, height);
        }
    }
    
}
