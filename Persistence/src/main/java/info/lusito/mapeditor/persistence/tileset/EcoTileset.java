package info.lusito.mapeditor.persistence.tileset;

import info.lusito.mapeditor.persistence.common.EcoImageDefinition;
import info.lusito.mapeditor.persistence.common.EcoSize;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.converters.extended.NamedMapConverter;
import info.lusito.mapeditor.persistence.utils.XStreamWrapped;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@XStreamAlias("tileset")
public class EcoTileset {

    @XStreamAsAttribute
    public String name;

    public EcoImageDefinition image;
    public EcoSize grid;
    public EcoSize margin;
    public EcoSize padding;

    @XStreamConverter(value = NamedMapConverter.class,
            strings = {"property", "key", "value"},
            booleans = {true, true},
            types = {String.class, String.class}, useImplicitType = false)
    public Map<String, String> properties;

    @XStreamImplicit
    public List<EcoTileInfo> tiles;

    @XStreamImplicit
    public List<EcoTerrain> terrains;

    @XStreamOmitField
    public Object attachment; // for user attachments

    public EcoTileInfo getTileInfo(int x, int y, boolean create) {
        //fixme: validate x/y within bounds
        //fixme: make more effecient
        if (tiles != null) {
            for (EcoTileInfo tile : tiles) {
                if (tile.x == x && tile.y == y) {
                    return tile;
                }
            }
        }
        if (create) {
            if (tiles == null) {
                tiles = new ArrayList();
            }
            EcoTileInfo tile = new EcoTileInfo();
            tile.x = x;
            tile.y = y;
            tile.tileset = this;
            tile.updateCoords();
            tiles.add(tile);
            return tile;
        }
        return null;
    }

    public void update(float stateTime) {
        //fixme: update tile animations
    }

    public void save(OutputStream stream) throws IOException {
        getXStream().toXML(this, stream);
    }

    public static EcoTileset load(InputStream stream) throws IOException {
        final EcoTileset tileset = (EcoTileset) getXStream().fromXML(stream);
        //fixme: validate

        if (tileset.tiles != null) {
            for (EcoTileInfo tile : tileset.tiles) {
                tile.tileset = tileset;
                tile.updateCoords();
            }
        }
        return tileset;
    }

    private static XStreamWrapped<EcoTileset> getXStream() {
        return new XStreamWrapped().processAnnotations(EcoTileset.class, EcoTileInfo.class);
    }

}
