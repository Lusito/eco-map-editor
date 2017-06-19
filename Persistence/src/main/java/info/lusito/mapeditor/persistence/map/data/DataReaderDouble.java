package info.lusito.mapeditor.persistence.map.data;

import info.lusito.mapeditor.persistence.common.EcoCompressionType;
import java.io.IOException;

public class DataReaderDouble extends DataReader {

    public DataReaderDouble(String data, EcoCompressionType compression) throws IOException {
        super(data, compression);
    }

    @Override
    public int read() throws IOException {
        return stream.read() | (stream.read() << 8);
    }
}
