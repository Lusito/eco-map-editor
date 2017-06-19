package info.lusito.mapeditor.editors.map.tools.select;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Batch;
import info.lusito.mapeditor.sharedlibgdx.camera.SimpleCamera;
import info.lusito.mapeditor.editors.map.model.EcoMapFX;
import info.lusito.mapeditor.editors.map.tools.AbstractTool;
import info.lusito.mapeditor.persistence.map.EcoTileLayer;

public class SelectTool extends AbstractTool {
    private final SelectInputHandler input;

    public SelectTool(SimpleCamera camera, EcoMapFX mapFX, EcoTileLayer drawingLayer) {
        super(camera, mapFX, createButton("selection", "Select, Move, Rotate tiles or objects", "Select"));
        input = new SelectInputHandler(this, camera);
    }

    public InputProcessor getInput() {
        return input;
    }
    
    public void setSelectionListener(SelectToolListener listener) {
        input.setSelectionListener(listener);
    }
    
    public void render(Batch batch, float zoomScale) {
        input.render(batch, zoomScale);
    }
}
