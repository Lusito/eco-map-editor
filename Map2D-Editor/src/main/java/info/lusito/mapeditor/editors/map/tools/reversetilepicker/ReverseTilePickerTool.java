package info.lusito.mapeditor.editors.map.tools.reversetilepicker;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import info.lusito.mapeditor.sharedlibgdx.camera.SimpleCamera;
import info.lusito.mapeditor.editors.map.model.EcoMapFX;
import info.lusito.mapeditor.editors.map.model.EcoMapLayerFX;
import info.lusito.mapeditor.editors.map.tools.AbstractTool;
import info.lusito.mapeditor.persistence.map.EcoMapLayer;
import info.lusito.mapeditor.persistence.map.EcoMapLayerType;
import info.lusito.mapeditor.persistence.map.EcoTileLayer;
import info.lusito.mapeditor.persistence.tileset.EcoTileInfo;

public class ReverseTilePickerTool extends AbstractTool {
    
    private final Vector2 dummyVectorA = new Vector2();
    private final ReversePickTileInputHandler input = new ReversePickTileInputHandler(this);

    public ReverseTilePickerTool(SimpleCamera camera, EcoMapFX mapFX, EcoTileLayer drawingLayer) {
        super(camera, mapFX, createButton("pipette", "Pick tile", "Pick tile"));
        button.setVisible(false); // disabled until it's clear how to get it working nicely
    }

    public InputProcessor getInput() {
        return input;
    }

    void pickTile(int screenX, int screenY) {
        // convert to map coordinates
        dummyVectorA.set(screenX, screenY);
        camera.unproject(dummyVectorA);
        int aX = (int) (dummyVectorA.x / map.tileSize.x);
        int aY = (int) (dummyVectorA.y / map.tileSize.y);
        if (aX < 0 || aY < 0 || aX >= map.size.x || aY >= map.size.y) {
            return;
        }
        final EcoTileLayer layer = getFocusTileLayer();
        if (layer != null) {
            EcoTileInfo tileInfo = layer.tiles[aX][aY];
            if(tileInfo != null) {
                System.out.println(tileInfo.tileset.name + tileInfo.x + '-' + tileInfo.y);
                //fixme: select in tilesetsview
            }
        }
    }

    protected EcoTileLayer getFocusTileLayer() {
        EcoMapLayerFX focusLayerFx = mapFX.getFocusLayerFX();
        if (focusLayerFx != null) {
            EcoMapLayer focusLayer = focusLayerFx.getLayer();
            if (focusLayer.getType() == EcoMapLayerType.TILE) {
                return (EcoTileLayer) focusLayer;
            }
        }
        return null;
    }
}
