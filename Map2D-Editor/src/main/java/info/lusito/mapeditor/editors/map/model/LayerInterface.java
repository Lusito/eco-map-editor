package info.lusito.mapeditor.editors.map.model;

import info.lusito.mapeditor.persistence.map.EcoMapLayerType;

public interface LayerInterface {

    EcoMapLayerType getType();

    String getName();

    void setName(String value);

    boolean isLocked();

    void setLocked(boolean value);

    boolean isVisible();

    void setVisible(boolean value);

    float getOpacity();

    void setOpacity(float value);

    void focus();
    
    void showProperties();
    
    void highlight();

    void duplicate();

    void remove();
}
