package info.lusito.mapeditor.persistence.map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@XStreamAlias("entity-layer")
public class EcoEntityLayer extends EcoMapLayer {

    @XStreamImplicit
    public List<EcoMapEntity> entities;

    public EcoEntityLayer() {
        readResolve();
    }

    public EcoEntityLayer(EcoEntityLayer layer) {
        this(layer, false);
    }

    EcoEntityLayer(EcoEntityLayer layer, boolean lightweight) {
        super(layer);
        entities = new ArrayList();
        for (EcoMapEntity entity : layer.entities) {
            entities.add(new EcoMapEntity(entity, lightweight));
        }
    }

    @Override
    EcoMapLayer createLightweightCopy() {
        return new EcoEntityLayer(this, true);
    }

    public EcoMapLayerType getType() {
        return EcoMapLayerType.ENTITY;
    }

    private Object readResolve() {
        if(properties == null){
            properties = new HashMap();
        }
        if(entities == null){
            entities = new ArrayList();
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
