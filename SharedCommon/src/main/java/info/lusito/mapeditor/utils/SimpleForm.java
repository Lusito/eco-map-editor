package info.lusito.mapeditor.utils;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SpringLayout.Constraints;

public final class SimpleForm extends JPanel {

    private static final int PAD_X = 5;
    private static final int PAD_Y = 5;

    public static class Entry {

        private final JLabel label;
        private final JTextField textField;

        public Entry(String text, String defaultValue, int textFieldColumns) {
            this.label = new JLabel(text);
            textField = new JTextField(textFieldColumns);
            textField.setText(defaultValue);
            label.setLabelFor(textField);
        }

        public String getText() {
            return textField.getText();
        }

        public Integer getInteger() {
            String value = textField.getText();
            if (value != null && !value.isEmpty()) {
                try {
                    return Integer.valueOf(value);
                } catch (NumberFormatException e) {
                }
            }
            return null;
        }

        public Float getFloat() {
            String value = textField.getText();
            if (value != null && !value.isEmpty()) {
                try {
                    return Float.valueOf(value);
                } catch (NumberFormatException e) {
                }
            }
            return null;
        }
    }

    public SimpleForm(Entry[] entries) {
        SpringLayout layout = new SpringLayout();
        setLayout(layout);

        JLabel[] labels = new JLabel[entries.length];
        JTextField[] textFields = new JTextField[entries.length];
        for (int i = 0; i < entries.length; i++) {
            Entry entry = entries[i];
            add(entry.label);
            add(entry.textField);
            labels[i] = entry.label;
            textFields[i] = entry.textField;
        }

        Spring y = Spring.constant(PAD_Y);
        for (int i = 0; i < entries.length; i++) {
            Spring height = Spring.max(
                    layout.getConstraints(labels[i]).getHeight(),
                    layout.getConstraints(textFields[i]).getHeight()
            );
            setupComponent(layout, labels[i], y, height);
            setupComponent(layout, textFields[i], y, height);
            y = Spring.sum(y, Spring.sum(height, Spring.constant(PAD_Y)));
        }

        Spring x = Spring.constant(PAD_X);
        x = Spring.sum(x, setupColumn(x, layout, labels));
        x = Spring.sum(x, setupColumn(x, layout, textFields));

        Constraints c = layout.getConstraints(this);
        c.setConstraint(SpringLayout.SOUTH, y);
        c.setConstraint(SpringLayout.EAST, x);
    }

    private void setupComponent(SpringLayout layout, Component component, Spring y, Spring height) {
        Constraints constraints = layout.getConstraints(component);
        constraints.setY(y);
        constraints.setHeight(height);
    }

    private Spring setupColumn(Spring x, SpringLayout layout, Component[] components) {
        Spring width = Spring.constant(0);
        for (Component component : components) {
            width = Spring.max(width, layout.getConstraints(component).getWidth());
        }
        for (Component component : components) {
            Constraints constraints
                    = layout.getConstraints(component);
            constraints.setX(x);
            constraints.setWidth(width);
        }
        return Spring.sum(width, Spring.constant(PAD_X));
    }
}
