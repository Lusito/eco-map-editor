package info.lusito.mapeditor.editors.map.utils;

import info.lusito.mapeditor.editors.map.MapEditorController;
import info.lusito.mapeditor.editors.map.model.EcoMapLayerFX;
import info.lusito.mapeditor.utils.undo.AbstractUndoableEditEDT;

public abstract class UndoableMapEdit extends AbstractUndoableEditEDT {

    protected final MapEditorController controller;
    protected final EcoMapLayerFX focusLayerFX;

    public UndoableMapEdit(MapEditorController controller) {
        super(controller);
        this.controller = controller;
        focusLayerFX = (EcoMapLayerFX)controller.getMap().getFocusLayer();
    }

    protected void applyLayerFocus(FocusMode mode) {
        controller.refocusLayer(focusLayerFX, mode);
    }
}
