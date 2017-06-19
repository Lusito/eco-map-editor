package info.lusito.mapeditor.sharedlibgdx.internal;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;

class TextureUtil {

    public static ReloadableTexture createStippleTexture(int w, int h, int dw, int dh) {
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fillRectangle(0, 0, dw, dh);
        final ReloadableTexture texture = createManagedTexture(pixmap);
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        return texture;
    }

    public static Pixmap createPixmap(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fillRectangle(0, 0, 1, 1);
        return pixmap;
    }

    public static ReloadableTexture createTexture(Color color) {
        return createManagedTexture(createPixmap(color));
    }

    public static ReloadableTexture createManagedTexture(Pixmap pixmap) {
        PixmapTextureData pixmapTextureData = new PixmapTextureData(pixmap, null, false, false, true);
        return new ReloadableTexture(pixmapTextureData);
    }

    public static TextureData loadSafe(FileHandle file) {
        if(file.exists()) {
            return TextureData.Factory.loadFromFile(file, null, false);
        } else {
            return new PixmapTextureData(createPixmap(Color.PINK), null, false, false, true);
        }
    }
}
