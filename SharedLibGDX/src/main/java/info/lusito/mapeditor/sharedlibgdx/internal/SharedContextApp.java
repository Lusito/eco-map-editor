package info.lusito.mapeditor.sharedlibgdx.internal;

import com.badlogic.gdx.ApplicationListener;

public class SharedContextApp implements ApplicationListener {
    
    private final Runnable runnable;

    public SharedContextApp(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void create() {
        runnable.run();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void render() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }
}
