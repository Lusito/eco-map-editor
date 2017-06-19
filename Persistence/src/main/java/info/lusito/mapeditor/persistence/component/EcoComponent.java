package info.lusito.mapeditor.persistence.component;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import info.lusito.mapeditor.persistence.utils.XStreamWrapped;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@XStreamAlias("component")
public class EcoComponent {

    @XStreamAsAttribute
    public String name;
    
    @XStreamAsAttribute
    public String description;

    @XStreamImplicit
    public List<EcoComponentProperty> properties;

    public EcoComponent() {
        readResolve();
    }

    private Object readResolve() {
        if(properties == null){
            properties = new ArrayList();
        }
        return this;
    }

    public void save(OutputStream stream) throws IOException {
        getXStream().toXML(this, stream);
    }
    
    public static EcoComponent load(InputStream stream) throws IOException {
        return getXStream().fromXML(stream);
    }

    private static XStreamWrapped<EcoComponent> getXStream() {
        return new XStreamWrapped().processAnnotations(EcoComponent.class, EcoComponentProperty.class);
    }
}
