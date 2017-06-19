package info.lusito.mapeditor.persistence.map.data;

import info.lusito.mapeditor.persistence.common.EcoCompressionType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class DataReader {

    protected final InputStream stream;

    public DataReader(String data, EcoCompressionType compression) throws IOException {
        byte[] dec = Base64.getDecoder().decode(data.trim());
        InputStream streamWrapper = new ByteArrayInputStream(dec);
        if(compression != null) {
            switch (compression) {
                case GZIP:
                    streamWrapper = new GZIPInputStream(streamWrapper);
                    break;
                case ZLIB:
                    streamWrapper = new InflaterInputStream(streamWrapper);
                    break;
            }
        }
        this.stream = streamWrapper;
    }

    public int read() throws IOException {
        return stream.read();
    }

    public int available() throws IOException {
        return stream.available();
    }
}
