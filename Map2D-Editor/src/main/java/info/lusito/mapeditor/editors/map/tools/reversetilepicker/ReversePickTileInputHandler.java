package info.lusito.mapeditor.editors.map.tools.reversetilepicker;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

public class ReversePickTileInputHandler extends InputAdapter {

    private boolean aborted;
    private final ReverseTilePickerTool tool;
    
    public ReversePickTileInputHandler(ReverseTilePickerTool tool) {
        this.tool = tool;
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
        aborted = true;
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            aborted = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            if(!aborted) {
                tool.pickTile(screenX, screenY);
            }
            return true;
        }
        return false;
    }
}
