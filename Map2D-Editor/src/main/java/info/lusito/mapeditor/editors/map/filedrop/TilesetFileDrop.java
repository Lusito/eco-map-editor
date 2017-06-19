package info.lusito.mapeditor.editors.map.filedrop;

import info.lusito.mapeditor.editors.map.model.EcoMapFX;
import org.openide.filesystems.FileObject;

public class TilesetFileDrop implements FileDropListener {

    private final EcoMapFX mapFX;

    public TilesetFileDrop(EcoMapFX mapFX) {
        this.mapFX = mapFX;
    }

    @Override
    public boolean onFileDrop(FileObject fileObject, String relativePath, float x, float y) {
        String ext = fileObject.getExt();
        if (ext.equalsIgnoreCase("xtd")) {
            mapFX.addTileset(relativePath);
            return true;
        }
        return false;
    }

}
