package info.lusito.mapeditor.sharedlibgdx;

import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.graphics.Texture;
import info.lusito.mapeditor.projecttype.GameProject;

public interface SharedContextProvider {

    LwjglAWTCanvas getCanvas();

    void init(Runnable callback);

    void pushContext();

    void popContext();

    Texture getTexture(String absolutePath);

//    void renameTexture(String absolutePath, String newAbsolutePath);
    Texture getWhiteTexture();

    Texture getStippleTexture(int w, int h, int dw, int dh);

    void freeTexture(Texture texture);

    void reloadTexture(String absolutePath);

    void reloadAllTextures();

    void addImageReloader(GameProject project);
}
