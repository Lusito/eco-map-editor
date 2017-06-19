package info.lusito.mapeditor.editors.properties.api;

public enum PropertyType {
    STRING("String"),
    MULTILINE_STRING("MultilineString"),
    FLOAT("Float"),
    INTEGER("Integer"),
    FILE("File"),
    COLOR("Color"),
    //ENTITY_LINK("Entity Link"),
    //VECTOR2("2D Vector"),//fixme: later.. hbox(TextField, TextField) ?
    //FLAGS("Flags"),//fixme: later.. hbox(label, button) => shows dialog, label contains csv of flags
    SLIDER("Slider"),
    BOOLEAN("Boolean"),
    ENUM("Enum");

    private final String label;

    PropertyType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
    
    public static PropertyType getSafe(String name) {
        for (PropertyType value : values()) {
            if(value.name().equalsIgnoreCase(name))
                return value;
        }
        return STRING;
    }
}
