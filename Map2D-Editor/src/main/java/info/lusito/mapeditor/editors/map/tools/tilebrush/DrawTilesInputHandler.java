package info.lusito.mapeditor.editors.map.tools.tilebrush;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

public class DrawTilesInputHandler extends InputAdapter {

    private int dragStartX, dragStartY;
    private boolean drawing;
    private final TileBrushTool tool;
    private boolean lineMode;
    
    public DrawTilesInputHandler(TileBrushTool tool) {
        this.tool = tool;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            stopDrag(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (drawing) {
            //fixme: linedraw preview?
            if(!lineMode)
                doDrag(screenX, screenY);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT || button == Input.Buttons.RIGHT) {
            lineMode = button == Input.Buttons.RIGHT;
            initDrag(screenX, screenY);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT || lineMode && button == Input.Buttons.RIGHT) {
            if(lineMode)
                doDrag(screenX, screenY);
            stopDrag(true);
            return true;
        }
        return false;
    }

    private void doDrag(int screenX, int screenY) {
        tool.drawTiles(dragStartX, dragStartY, screenX, screenY);
        dragStartX = screenX;
        dragStartY = screenY;
    }

    private void initDrag(int screenX, int screenY) {
        if (!drawing) {
            dragStartX = screenX;
            dragStartY = screenY;
            drawing = true;
            tool.startDrawTiles(screenX, screenY);
        }
    }

    private void stopDrag(boolean accept) {
        if(drawing) {
            drawing = false;
            tool.stopDrawTiles(accept);
        }
    }

}
