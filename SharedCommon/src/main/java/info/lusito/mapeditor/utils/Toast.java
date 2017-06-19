package info.lusito.mapeditor.utils;

import info.lusito.mapeditor.common.EcoIcons;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.NotificationDisplayer.Priority;
import org.openide.awt.NotificationDisplayer.Category;
import org.openide.util.ImageUtilities;

public class Toast {

    public enum Type {
        INFO(EcoIcons.INFO, Priority.LOW, Category.INFO),
        WARNING(EcoIcons.WARNING, Priority.NORMAL, Category.WARNING),
        ERROR(EcoIcons.ERROR, Priority.HIGH, Category.ERROR);

        final Icon icon;
        final Priority priority;
        final Category category;

        Type(String iconPath, Priority priority, Category category) {
            this.icon = new ImageIcon(ImageUtilities.loadImage(iconPath));
            this.priority = priority;
            this.category = category;
        }
    }

    public static void show(Type type, String title, String message, ActionListener action) {
        NotificationDisplayer nd = NotificationDisplayer.getDefault();
        nd.notify(title, type.icon, message, action, type.priority, type.category);
    }

    public static void showInfo(String title, String message, ActionListener action) {
        show(Type.INFO, title, message, action);
    }

    public static void showWarning(String title, String message, ActionListener action) {
        show(Type.WARNING, title, message, action);
    }

    public static void showError(String title, String message, ActionListener action) {
        show(Type.ERROR, title, message, action);
    }
}
