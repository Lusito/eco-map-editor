package info.lusito.mapeditor.persistence.map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("image")
public class EcoMapImage {

    @XStreamAsAttribute
    public float x;

    @XStreamAsAttribute
    public float y;

    @XStreamAsAttribute
    public float scale = 1;

    @XStreamAsAttribute
    public float rotation;

    @XStreamAsAttribute
    public String tint;

    @XStreamAsAttribute
    public String filename;

    @XStreamOmitField
    public Object attachment; // for user attachments

    public EcoMapImage(EcoMapImage image) {
        this.x = image.x;
        this.y = image.y;
        this.scale = image.scale;
        this.rotation = image.rotation;
        this.tint = image.tint;
        this.filename = image.filename;
    }

    public EcoMapImage() {
    }
}
