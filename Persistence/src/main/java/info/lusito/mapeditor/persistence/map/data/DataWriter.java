package info.lusito.mapeditor.persistence.map.data;

import info.lusito.mapeditor.persistence.common.EcoCompressionType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

public class DataWriter {

    protected final OutputStream stream;
    private final ByteArrayOutputStream byteStream;

    public DataWriter(EcoCompressionType compression, int size) throws IOException {
        byteStream = new ByteArrayOutputStream(size);
        OutputStream streamWrapper = byteStream;
        if(compression != null) {
            switch (compression) {
                case GZIP:
                    streamWrapper = new GZIPOutputStream(streamWrapper);
                    break;
                case ZLIB:
                    streamWrapper = new DeflaterOutputStream(streamWrapper);
                    break;
            }
        }
        stream = streamWrapper;
    }

    public void write(int value) throws IOException {
        stream.write(value & 0xFF);
    }

    public String finish() throws IOException {
        stream.flush();
        if(stream instanceof GZIPOutputStream)
            ((GZIPOutputStream)stream).finish();
        else if(stream instanceof DeflaterOutputStream)
            ((DeflaterOutputStream)stream).finish();
        return Base64.getEncoder().encodeToString(byteStream.toByteArray());
    }
}
