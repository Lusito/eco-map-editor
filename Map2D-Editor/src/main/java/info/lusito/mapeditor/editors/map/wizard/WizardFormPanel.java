package info.lusito.mapeditor.editors.map.wizard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class WizardFormPanel extends JPanel {

    private static final long serialVersionUID = 7744271719615675700L;
    
    private final CustomDocumentListener documentListener = new CustomDocumentListener();
    private int rows;

    public WizardFormPanel() {
        GridBagLayout panelGridBagLayout = new GridBagLayout();
        panelGridBagLayout.columnWidths = new int[]{86, 86, 0};
        panelGridBagLayout.rowHeights = new int[]{20, 20, 20, 20, 20, 0};
        panelGridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        panelGridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(panelGridBagLayout);
    }
    
    protected abstract void onChange();

    protected JTextField addLabelAndTextField(String labelText, String value) {

        int rowIndex = rows++;
        JLabel label = new JLabel(labelText);
        GridBagConstraints gridBagConstraintForLabel = new GridBagConstraints();
        gridBagConstraintForLabel.fill = GridBagConstraints.BOTH;
        gridBagConstraintForLabel.insets = new Insets(0, 0, 5, 5);
        gridBagConstraintForLabel.gridx = 0;
        gridBagConstraintForLabel.gridy = rowIndex;
        add(label, gridBagConstraintForLabel);

        JTextField textField = new JTextField(value);
        GridBagConstraints gridBagConstraintForTextField = new GridBagConstraints();
        gridBagConstraintForTextField.fill = GridBagConstraints.BOTH;
        gridBagConstraintForTextField.insets = new Insets(0, 0, 5, 0);
        gridBagConstraintForTextField.gridx = 1;
        gridBagConstraintForTextField.gridy = rowIndex;
        add(textField, gridBagConstraintForTextField);
        textField.setColumns(10);
        textField.getDocument().addDocumentListener(documentListener);
        return textField;
    }
    
    public class CustomDocumentListener implements DocumentListener {
        @Override
        public void changedUpdate(DocumentEvent e) {
            onChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            onChange();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            onChange();
        }
    }
}
