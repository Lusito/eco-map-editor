package info.lusito.mapeditor.utils;

import java.lang.reflect.Field;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.scene.control.Control;
import javafx.scene.control.TextInputControl;

public class UndoUtil {

    public static void removeUndoRedo(TextInputControl... ctrls) {
        for (Control ctrl : ctrls) {
            if (ctrl instanceof TextInputControl) {
                try {
                    Field field = TextInputControl.class.
                            getDeclaredField("undoable");
                    field.setAccessible(true);
                    ReadOnlyBooleanWrapper undoable = (ReadOnlyBooleanWrapper) field.get(ctrl);
                    undoable.addListener((v, o, n) -> {
                        if (n) {
                            undoable.set(false);
                        }
                    });
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
//                    ex.printStackTrace();
                }
            }
        }
    }
}
