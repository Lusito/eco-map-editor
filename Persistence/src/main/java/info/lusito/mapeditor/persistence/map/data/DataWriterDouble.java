package info.lusito.mapeditor.persistence.map.data;

import info.lusito.mapeditor.persistence.common.EcoCompressionType;
import java.io.IOException;

public class DataWriterDouble extends DataWriter {

    public DataWriterDouble(EcoCompressionType compression, int size) throws IOException {
        super(compression, size*2);
    }

    @Override
    public void write(int value) throws IOException {
        stream.write(value & 0xFF);
        stream.write((value & 0xFF00) >> 8);
    }
}
