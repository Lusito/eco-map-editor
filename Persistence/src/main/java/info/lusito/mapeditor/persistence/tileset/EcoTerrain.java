package info.lusito.mapeditor.persistence.tileset;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;
import info.lusito.mapeditor.persistence.common.EcoCompressionType;
import info.lusito.mapeditor.persistence.map.data.DataReader;
import info.lusito.mapeditor.persistence.map.data.DataWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@XStreamAlias("terrain")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {"data"})
public class EcoTerrain {

    @XStreamAsAttribute
    public String name;

    private String data;

    @XStreamOmitField
    public Set<Integer> quarterIds;
    
    public void setData(EcoCompressionType compression, Set<Integer> quarterIds) throws IOException {
        DataWriter writer = new DataWriter(compression, quarterIds.size() * 4);
        for (Integer quarterId : quarterIds) {
            writer.write(quarterId & 0xFF);
            writer.write((quarterId & 0xFF00) >> 8);
            writer.write((quarterId & 0xFF0000) >> 16);
            writer.write((quarterId & 0xFF000000) >> 24);
        }
        data = writer.finish();
    }
    
    public Set<Integer> getData(EcoCompressionType compression) throws IOException {
        Set<Integer> set = new HashSet();
        DataReader reader = new DataReader(data, compression);
        while(reader.available() > 0) {
            set.add(reader.read() | (reader.read() << 8)
                     | (reader.read() << 16) | (reader.read() << 24));
        }
        return set;
    }
}
