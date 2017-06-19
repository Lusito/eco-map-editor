package info.lusito.mapeditor.editors.map.tools.polyline;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

public class DrawPolylineInputHandler extends InputAdapter {

    private boolean aborted;
    private final DrawPolylineTool tool;
    
    public DrawPolylineInputHandler(DrawPolylineTool tool) {
        this.tool = tool;
    }

    public boolean isAborted() {
        return aborted;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            aborted = true;
            tool.stop(0, 0, false);
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
            return true;
        } else if(button == Input.Buttons.RIGHT) {
            tool.stop(screenX, screenY, !aborted);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            tool.add(screenX, screenY);
            return true;
        }
        aborted = true;
        return false;
    }
}
