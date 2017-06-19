package info.lusito.mapeditor.persistence.map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.converters.extended.NamedMapConverter;
import info.lusito.mapeditor.persistence.common.EcoCompressionType;
import info.lusito.mapeditor.persistence.common.EcoSize;
import info.lusito.mapeditor.persistence.shape.EcoCircle;
import info.lusito.mapeditor.persistence.shape.EcoPolygon;
import info.lusito.mapeditor.persistence.shape.EcoPolyline;
import info.lusito.mapeditor.persistence.shape.EcoRectangle;
import info.lusito.mapeditor.persistence.shape.EcoShape;
import info.lusito.mapeditor.persistence.tileset.EcoTileset;
import info.lusito.mapeditor.persistence.utils.XStreamWrapped;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XStreamAlias("map")
public class EcoMap {

    @XStreamAsAttribute
    public String name;

    @XStreamAsAttribute
    public EcoCompressionType compression;
    
    public EcoSize size;
    
    @XStreamAlias("tile-size")
    public EcoSize tileSize;

    @XStreamConverter(value = NamedMapConverter.class,
            strings = { "property", "key", "value" },
            booleans = { true, true },
            types = { String.class, String.class }, useImplicitType = false)
    public Map<String, String> properties;

    @XStreamImplicit
    public List<EcoMapTilesetReference> tilesets;
    
    @XStreamImplicit
    public List<EcoMapLayer> layers;

    public EcoMap() {
        readResolve();
    }

    private EcoMap createLighweightCopy() {
        EcoMap map = new EcoMap();
        map.name = name;
        map.compression = compression;
        map.size = size;
        map.tileSize = tileSize;
        map.properties = properties;
        map.tilesets = tilesets;
        for (EcoMapLayer layer : layers) {
            map.layers.add(layer.createLightweightCopy());
        }

        return map;
    }

    public void save(OutputStream stream) throws IOException {
        EcoMap copy = createLighweightCopy();
        for (EcoMapLayer layer : copy.layers) {
            if(layer instanceof EcoTileLayer) {
                EcoTileLayer tileLayer = (EcoTileLayer)layer;
                tileLayer.onBeforeWrite(this);
            }
        }
        getXStream().toXML(copy, stream);

        for (EcoMapLayer layer : copy.layers) {
            if(layer instanceof EcoTileLayer) {
                EcoTileLayer tileLayer = (EcoTileLayer)layer;
                tileLayer.onAfterWrite();
            }
        }
    }

    private Object readResolve() {
        if(properties == null){
            properties = new HashMap();
        }
        if(tilesets == null){
            tilesets = new ArrayList();
        }
        if(layers == null){
            layers = new ArrayList();
        }
        if(compression == null) {
            compression = EcoCompressionType.NONE;
        }
        return this;
    }

    public static EcoMap load(InputStream stream) throws IOException {
        return (EcoMap) getXStream().fromXML(stream);
    }

    private static XStreamWrapped<EcoMap> getXStream() {
        return new XStreamWrapped()
                .processAnnotations(EcoMap.class, EcoEntityLayer.class, EcoImageLayer.class,
                        EcoTileLayer.class, EcoMapEntity.class, EcoMapImage.class,
                        EcoMapEntityComponent.class, EcoMapTilesetReference.class)
                .addDefaultImplementations(EcoShape.class, EcoCircle.class, EcoRectangle.class,
                        EcoPolygon.class, EcoPolyline.class);
    }

    public EcoTileset getTileset(int index) {
        if(index >= tilesets.size())
            return null;
        return tilesets.get(index).tileset;
    }

    int getTilesetId(EcoTileset tileset) {
        int i=0;
        for (EcoMapTilesetReference tsRef : tilesets) {
            if(tsRef.tileset == tileset)
                return i;
            i++;
        }
        return -1;
    }
}
