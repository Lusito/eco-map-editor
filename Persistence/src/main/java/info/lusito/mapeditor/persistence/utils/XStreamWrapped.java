package info.lusito.mapeditor.persistence.utils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class XStreamWrapped<T> {

    private final XStream xstream = new XStream(new PureJavaReflectionProvider());

    public XStreamWrapped processAnnotations(Class... annotationTypes) {
        xstream.processAnnotations(annotationTypes);
        return this;
    }

    public XStreamWrapped addDefaultImplementations(Class base, Class... classes) {
        for (Class clazz : classes) {
            xstream.addDefaultImplementation(clazz, base);
        }
        return this;
    }

    public T fromXML(InputStream stream) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(stream, Charset.forName("UTF-8"))) {
            return (T) xstream.fromXML(reader);
        } catch (XStreamException e) {
            throw new IOException(e);
        }
    }

    public void toXML(T object, OutputStream stream) throws IOException {
        try(OutputStreamWriter writer = new OutputStreamWriter(stream, Charset.forName("UTF-8"))) {
            xstream.toXML(object, writer);
        } catch (XStreamException e) {
            throw new IOException(e);
        }
    }
}
