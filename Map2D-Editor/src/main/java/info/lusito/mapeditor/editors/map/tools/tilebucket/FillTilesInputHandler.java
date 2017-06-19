package info.lusito.mapeditor.editors.map.tools.tilebucket;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

public class FillTilesInputHandler extends InputAdapter {

    private int dragStartX, dragStartY;
    private boolean drawing;
    private final TileBucketTool tool;
    
    public FillTilesInputHandler(TileBucketTool tool) {
        this.tool = tool;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            drawing = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return drawing;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            if (!drawing) {
                dragStartX = screenX;
                dragStartY = screenY;
                drawing = true;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            if(drawing) {
                drawing = false;
                int diffX = Math.abs(dragStartX - screenX);
                int diffY = Math.abs(dragStartY - screenY);
                if(diffX < 3 && diffY < 3) {
                    tool.fillTiles(dragStartX, dragStartY);
                }
            }
            return true;
        }
        return false;
    }
}
