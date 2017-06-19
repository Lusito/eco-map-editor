package info.lusito.mapeditor.editors.map.tools.tilebrush;

import info.lusito.mapeditor.editors.map.MapEditorController;
import info.lusito.mapeditor.editors.map.utils.FocusMode;
import info.lusito.mapeditor.editors.map.utils.UndoableMapEdit;
import info.lusito.mapeditor.persistence.map.EcoTileLayer;
import info.lusito.mapeditor.persistence.tileset.EcoTileInfo;
import java.util.Arrays;
import javax.swing.undo.CannotRedoException;

public class UndoableTileLayerChange extends UndoableMapEdit {
    
    private final EcoTileLayer layer;
    private final EcoTileInfo[][] from;
    private final EcoTileInfo[][] to;

    public UndoableTileLayerChange(MapEditorController controller, EcoTileLayer layer, EcoTileInfo[][] from, EcoTileInfo[][] to) {
        super(controller);
        this.layer = layer;
        this.from = from;
        this.to = to;
    }

    private void performUndoRedo(EcoTileInfo[][] copy) {
        applyLayerFocus(FocusMode.LAYER);
        if(copy == null) {
            for (EcoTileInfo[] row : layer.tiles) {
                Arrays.fill(row, null);
            }
        } else {
            for (int i = 0; i < copy.length; i++) {
                System.arraycopy(copy[i], 0, layer.tiles[i], 0, layer.tiles[i].length);
            }
        }
    }

    @Override
    protected void performUndo() throws CannotRedoException {
        performUndoRedo(from);
    }

    @Override
    protected void performRedo() throws CannotRedoException {
        performUndoRedo(to);
    }

    public static EcoTileInfo[][] createCopy(EcoTileInfo[][] from) {
        EcoTileInfo[][] copy = new EcoTileInfo[from.length][from[0].length];
        for (int i = 0; i < copy.length; i++) {
            System.arraycopy(from[i], 0, copy[i], 0, from[i].length);
        }
        return copy;
    }
    
}
