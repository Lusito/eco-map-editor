package info.lusito.mapeditor.persistence.map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import info.lusito.mapeditor.persistence.tileset.EcoTileset;

@XStreamAlias("tileset-ref")
public class EcoMapTilesetReference {

    @XStreamAsAttribute
    public String src;
    
    @XStreamOmitField
    public EcoTileset tileset;
}
