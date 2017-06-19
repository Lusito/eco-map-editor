package info.lusito.mapeditor.editors.map.model;

import info.lusito.mapeditor.persistence.common.EcoCompressionType;
import info.lusito.mapeditor.persistence.tileset.EcoTerrain;
import info.lusito.mapeditor.persistence.tileset.EcoTileInfo;
import info.lusito.mapeditor.persistence.tileset.EcoTileset;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TerrainInfo {

    private final List<String> terrainNames = new ArrayList();
    public final Map<Integer, List<EcoTileInfo>> terrainMap = new HashMap();
    
    public void prepareTileset(EcoTileset tileset, EcoCompressionType compression) throws IOException {
        for (EcoTerrain terrain : tileset.terrains) {
            terrain.quarterIds = terrain.getData(compression);
        }
    }

    public void addTileset(EcoTileset tileset) {
        int[][] terrainBits = new int[tileset.grid.x][tileset.grid.y];
        for (EcoTerrain terrain : tileset.terrains) {
            Set<Integer> quarterIds = terrain.quarterIds;
            int bitsIndex = terrainNames.indexOf(terrain.name);
            if (bitsIndex == -1) {
                terrainNames.add(terrain.name);
                bitsIndex = terrainNames.size();
            } else {
                bitsIndex++;
            }
            for (Integer quarter : quarterIds) {
                int qx = quarter & 0xFFFF;
                int qy = (quarter & 0xFFFF0000) >> 16;
                int tileX = qx / 2;
                int tileY = qy / 2;
                int value = terrainBits[tileX][tileY];
                boolean right = qx % 2 == 1;
                boolean bottom = qy % 2 == 1;
                if (!bottom && !right) {
                    value = (value & 0x00FFFFFF)
                            | ((bitsIndex << 24) & ~0x00FFFFFF);
                } else if (!bottom && right) {
                    value = (value & 0xFF00FFFF)
                            | ((bitsIndex << 16) & ~0xFF00FFFF);
                } else if (bottom && !right) {
                    value = (value & 0xFFFF00FF)
                            | ((bitsIndex << 8) & ~0xFFFF00FF);
                } else {
                    value = (value & 0xFFFFFF00)
                            | (bitsIndex & ~0xFFFFFF00);
                }
                terrainBits[tileX][tileY] = value;
            }
        }
        for (int y = 0; y < tileset.grid.y; y++) {
            for (int x = 0; x < tileset.grid.x; x++) {
                int bits = terrainBits[x][y];
                if (bits == 0) {
                    continue;
                }
                EcoTileInfo ti = tileset.getTileInfo(x, y, true);
                ti.terrainBits = bits;
                List<EcoTileInfo> list = terrainMap.get(bits);
                if (list == null) {
                    list = new ArrayList();
                    terrainMap.put(bits, list);
                }
                list.add(ti);
            }
        }
    }

    public void removeTileset(EcoTileset tileset) {
        for (List<EcoTileInfo> list : terrainMap.values()) {
            Iterator<EcoTileInfo> it = list.iterator();
            while (it.hasNext()) {
                EcoTileInfo tile = it.next();
                if (tile.tileset == tileset) {
                    it.remove();
                }
            }
        }
    }
}
