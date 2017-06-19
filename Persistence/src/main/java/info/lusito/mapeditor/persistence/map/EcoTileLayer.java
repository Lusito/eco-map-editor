package info.lusito.mapeditor.persistence.map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;
import info.lusito.mapeditor.persistence.map.data.DataReader;
import info.lusito.mapeditor.persistence.map.data.DataReaderDouble;
import info.lusito.mapeditor.persistence.map.data.DataWriter;
import info.lusito.mapeditor.persistence.map.data.DataWriterDouble;
import info.lusito.mapeditor.persistence.tileset.EcoTileInfo;
import info.lusito.mapeditor.persistence.tileset.EcoTileset;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

@XStreamAlias("tile-layer")
public class EcoTileLayer extends EcoMapLayer {

    private Content content;

    @XStreamOmitField
    public EcoTileInfo[][] tiles;

    public EcoTileLayer() {
        readResolve();
    }

    public EcoTileLayer(EcoTileLayer layer) {
        super(layer);
        readResolve();
        copyFrom(layer);
    }

    @Override
    EcoMapLayer createLightweightCopy() {
        return this;
    }

    private Object readResolve() {
        if(properties == null){
            properties = new HashMap();
        }
        return this;
    }
    
    public final void copyFrom(EcoTileLayer layer) {
        attachment = layer.attachment;
        int width = layer.tiles.length;
        int height = layer.tiles[0].length;
        tiles = new EcoTileInfo[width][height];
        for (int x = 0; x < width; x++) {
            System.arraycopy(layer.tiles[x], 0, tiles[x], 0, layer.tiles[x].length);
        }
    }

    @Override
    public EcoMapLayerType getType() {
        return EcoMapLayerType.TILE;
    }

    @Override
    void onBeforeWrite(EcoMap map) {
        content = new Content();
        content.indexSize = map.tilesets.size() >= 255 ? 2 : 1;

        try {
            // calculate number of values written and index size
            int calculatedSize = 0;
            for (int y = 0; y < map.size.y; y++) {
                for (int x = 0; x < map.size.x; x++) {
                    EcoTileInfo tile = tiles[x][y];
                    if (tile == null) {
                        calculatedSize++;
                    } else {
                        calculatedSize += 3;
                        if (content.indexSize == 1
                                && (tile.x >= 256 || tile.y >= 256)) {
                            content.indexSize = 2;
                        }
                    }
                }
            }
            writeContent(map, calculatedSize);
        } catch (Exception e) {
            throw new RuntimeException("Error writing tileset content", e);
        }
    }

    private void writeContent(EcoMap map, int calculatedSize) throws IOException {
        DataWriter writer;
        if (content.indexSize == 2) {
            writer = new DataWriterDouble(map.compression, calculatedSize);
        } else {
            writer = new DataWriter(map.compression, calculatedSize);
        }

        for (int y = 0; y < map.size.y; y++) {
            for (int x = 0; x < map.size.x; x++) {
                EcoTileInfo tile = tiles[x][y];
                if (tile == null) {
                    writer.write(0);
                } else {
                    int tsId = map.getTilesetId(tile.tileset);
                    // This should never actually happen
                    if (tsId == -1) {
                        throw new RuntimeException("Tileset not found");
                    }
                    writer.write(tsId + 1);
                    writer.write(tile.x);
                    writer.write(tile.y);
                }
            }
        }
        content.data = writer.finish();
    }

    @Override
    void onAfterWrite() {
        content = null;
    }

    @Override
    public void onAfterRead(EcoMap map) {
        tiles = new EcoTileInfo[map.size.x][map.size.y];
        if (content != null && content.data != null) {
            try {
                readContent(map);
            } catch (Exception e) {
                throw new RuntimeException("Error reading tileset content", e);
            }

            content = null;
        }
    }

    private void readContent(EcoMap map) throws IOException, Exception {
        DataReader reader;
        if (content.indexSize == 2) {
            reader = new DataReaderDouble(content.data, map.compression);
        } else {
            reader = new DataReader(content.data, map.compression);
        }

        int x = 0, y = 0;
        while (reader.available() > 0) {
            int tsId = reader.read();
            if (tsId > 0) {
                int tsX = reader.read();
                int tsY = reader.read();
                tsId--;
                EcoTileset tileset = map.getTileset(tsId);
                if (tileset == null) {
                    throw new RuntimeException("Invalid tileset id: " + tsId);
                }
                tiles[x][y] = tileset.getTileInfo(tsX, tsY, true);
            }
            x++;
            if (x >= map.size.x) {
                x = 0;
                y++;
                if (y >= map.size.y) {
                    if (reader.available() > 0) {
                        // fixme:.. seems to be sometimes without importance
//                        throw new RuntimeException("More tileset content available than there should be");
                    }
                    break;
                }
            }
        }
    }

    public void clearTiles() {
        for (EcoTileInfo[] row : tiles) {
             Arrays.fill(row, null);
        }
    }

    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"data"})
    private static class Content {

        @XStreamAsAttribute
        @XStreamAlias("index-size")
        private int indexSize = 1;

        private String data;
    }

}
