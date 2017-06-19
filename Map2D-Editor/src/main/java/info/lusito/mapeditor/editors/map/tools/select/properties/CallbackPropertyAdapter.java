package info.lusito.mapeditor.editors.map.tools.select.properties;

import info.lusito.mapeditor.editors.properties.api.adapters.PropertyAdapter;

public class CallbackPropertyAdapter extends PropertyAdapter {
    
    private final Callback callback;

    public CallbackPropertyAdapter(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void setValue(String value) {
        callback.setValue(this, value);
    }

    @Override
    public String getValue() {
        return callback.getValue(this);
    }
    
    
    public interface Callback {
        void setValue(PropertyAdapter adapter, String value);
        String getValue(PropertyAdapter adapter);
    }
}
