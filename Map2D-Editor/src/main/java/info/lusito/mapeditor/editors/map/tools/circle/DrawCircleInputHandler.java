package info.lusito.mapeditor.editors.map.tools.circle;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

public class DrawCircleInputHandler extends InputAdapter {

    private boolean aborted;
    private final DrawCircleTool tool;
    
    public DrawCircleInputHandler(DrawCircleTool tool) {
        this.tool = tool;
    }

    public boolean isAborted() {
        return aborted;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            aborted = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(!aborted) {
            tool.update(screenX, screenY);
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            aborted = false;
            tool.start(screenX, screenY);
            return true;
        }
        aborted = true;
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            tool.stop(screenX, screenY, !aborted);
            return true;
        }
        aborted = true;
        return false;
    }
}
