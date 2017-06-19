package info.lusito.mapeditor.persistence.map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@XStreamAlias("image-layer")
public class EcoImageLayer extends EcoMapLayer {

    @XStreamImplicit
    public List<EcoMapImage> images;

    public EcoImageLayer() {
        readResolve();
    }

    public EcoImageLayer(EcoImageLayer layer) {
        super(layer);

        images = new ArrayList(layer.images.size());
        for (EcoMapImage image : layer.images) {
            images.add(new EcoMapImage(image));
        }
        readResolve();
    }

    @Override
    EcoMapLayer createLightweightCopy() {
        return this;
    }

    public EcoMapLayerType getType() {
        return EcoMapLayerType.IMAGE;
    }

    private Object readResolve() {
        if(properties == null){
            properties = new HashMap();
        }
        if(images == null){
            images = new ArrayList();
        }
        return this;
    }

    @Override
    public void onAfterRead(EcoMap map) {
    }

    @Override
    void onBeforeWrite(EcoMap map) {
    }

    @Override
    void onAfterWrite() {
    }
}
