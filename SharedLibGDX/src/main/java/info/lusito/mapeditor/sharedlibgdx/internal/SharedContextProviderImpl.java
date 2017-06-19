package info.lusito.mapeditor.sharedlibgdx.internal;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import info.lusito.mapeditor.projecttype.GameProject;
import info.lusito.mapeditor.service.filewatcher.FileWatcher;
import info.lusito.mapeditor.sharedlibgdx.SharedContextProvider;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SharedContextProvider.class)
public class SharedContextProviderImpl implements SharedContextProvider {

    private LwjglAWTCanvas canvas;
    private SharedContextWindow frame;
    private SharedContextApp app;
    private Application lastContext;
    private int pushed;
    private ReloadableTexture white;
    private Set<Runnable> readyCallbacks = ConcurrentHashMap.newKeySet();
    private final Map<String, ReloadableTexture> textures = new ConcurrentHashMap();
    private final Map<GameProject, ImageReloader> imageReloaders = new ConcurrentHashMap();

    @Override
    public LwjglAWTCanvas getCanvas() {
        return canvas;
    }

    private void onReady() {
        white = TextureUtil.createTexture(Color.WHITE);
        for (Runnable runnable : readyCallbacks) {
            runnable.run();
        }
        readyCallbacks.clear();
        readyCallbacks = null;
    }

    @Override
    public void init(Runnable callback) {
        if (readyCallbacks == null) {
            callback.run();
        } else {
            if (canvas == null) {
                LwjglApplicationConfiguration.disableAudio = true;
                app = new SharedContextApp(this::onReady);
                canvas = new LwjglAWTCanvas(app);
                frame = new SharedContextWindow(canvas);
            }
            readyCallbacks.add(callback);
        }
    }

    @Override
    public void pushContext() {
        if (pushed == 0) {
            lastContext = Gdx.app;
        }
        pushed++;
    }

    @Override
    public void popContext() {
        pushed--;
        if (pushed == 0) {
            if (lastContext != canvas && lastContext instanceof LwjglAWTCanvas) {
                ((LwjglAWTCanvas) lastContext).makeCurrent();
                lastContext = null;
            }
        }
    }

    @Override
    public Texture getTexture(String absolutePath) {
        absolutePath = absolutePath.replace('\\', '/');
        ReloadableTexture texture = textures.get(absolutePath);
        if (texture == null) {
            //fixme: reload textures on change?
            final FileHandle file = Gdx.files.absolute(absolutePath);
            texture = new ReloadableTexture(file);
            textures.put(absolutePath, texture);
        }
        texture.useCount++;
        return texture;
    }

//    @Override
    // For later when refactoring
    public void renameTexture(String absolutePath, String newAbsolutePath) {
        ReloadableTexture texture = textures.get(absolutePath);
        if (texture != null) {
            final FileHandle file = Gdx.files.absolute(newAbsolutePath);
            texture.setFile(file);
            textures.remove(absolutePath);
            textures.put(absolutePath, texture);
        }
    }

    public Texture getWhiteTexture() {
        return white;
    }

    @Override
    public Texture getStippleTexture(int w, int h, int dw, int dh) {
        String key = "st:" + w + "-" + h + "-" + dw + "-" + dh + "-";
        ReloadableTexture texture = textures.get(key);
        if (texture == null) {
            texture = TextureUtil.createStippleTexture(w, h, dw, dh);
            textures.put(key, texture);
        }
        texture.useCount++;
        return texture;
    }

    @Override
    public void freeTexture(Texture texture) {
        for (Map.Entry<String, ReloadableTexture> entry : textures.entrySet()) {
            ReloadableTexture texture2 = entry.getValue();
            if (texture2 == texture) {
                texture2.useCount--;
                if (texture2.useCount == 0) {
                    textures.remove(entry.getKey());
                    texture2.dispose();
                }
                break;
            }
        }
    }

    @Override
    public void reloadTexture(String absolutePath) {
        ReloadableTexture texture = textures.get(absolutePath);
        if (texture != null) {
            texture.reloadFromDisc();
        }
    }

    @Override
    public void reloadAllTextures() {
        for (ReloadableTexture value : textures.values()) {
            value.reloadFromDisc();
        }
        Texture.invalidateAllTextures(canvas);
    }

    @Override
    public void addImageReloader(GameProject project) {
        if (!imageReloaders.containsKey(project)) {
            FileWatcher fileWatcher = project.getFileWatcher("*");
            ImageReloader imageReloader = new ImageReloader(this);
            fileWatcher.addListener(imageReloader);
            imageReloaders.put(project, imageReloader);
        }
    }
}
