package info.lusito.mapeditor.persistence.animation;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import info.lusito.mapeditor.persistence.common.EcoSize;
import info.lusito.mapeditor.persistence.common.EcoImageDefinition;
import info.lusito.mapeditor.persistence.utils.XStreamWrapped;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@XStreamAlias("animation")
public class EcoAnimation {

    public EcoImageDefinition image;
    public EcoSize grid;
    public ClipDefinition clip;

    public void save(OutputStream stream) throws IOException {
        getXStream().toXML(this, stream);
    }

    public static EcoAnimation load(InputStream stream) throws IOException {
        final EcoAnimation animation = getXStream().fromXML(stream);
        //fixme: validate

        return animation;
    }

    private static XStreamWrapped<EcoAnimation> getXStream() {
        return new XStreamWrapped().processAnnotations(EcoAnimation.class);
    }

    public static class ClipDefinition {

        @XStreamAsAttribute
        public String frames;
        @XStreamAsAttribute
        public String durations;
        @XStreamAsAttribute
        public Mode mode;
    }

    public static enum Mode {
        NORMAL,
        REVERSED,
        LOOP,
        LOOP_REVERSED,
        LOOP_PINGPONG,
        LOOP_RANDOM
    }
}
