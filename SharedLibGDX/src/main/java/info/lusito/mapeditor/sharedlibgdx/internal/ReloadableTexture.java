package info.lusito.mapeditor.sharedlibgdx.internal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ReloadableTexture extends Texture {
    private FileHandle file;
    int useCount;

	public ReloadableTexture (FileHandle file) {
		super(TextureUtil.loadSafe(file));
        this.file = file;
	}

	public ReloadableTexture (TextureData data) {
		super(data);
	}
    
	public void reloadFromDisc () {
		if (!isManaged()) throw new GdxRuntimeException("Tried to reload unmanaged Texture");
        delete();
		glHandle = Gdx.gl.glGenTexture();
        TextureData textureData = getTextureData();
        if(file != null) {
            if(textureData instanceof PixmapTextureData) {
                if(file.exists())
                    textureData = TextureUtil.loadSafe(file);
            } else if(!file.exists()) {
                textureData = new PixmapTextureData(TextureUtil.createPixmap(Color.PINK), null, false, false, true);
            }
        }
		load(textureData);
	}

    void setFile(FileHandle file) {
        this.file = file;
        reloadFromDisc();
    }
}
