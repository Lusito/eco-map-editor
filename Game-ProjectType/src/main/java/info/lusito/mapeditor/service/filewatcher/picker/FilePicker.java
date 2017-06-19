package info.lusito.mapeditor.service.filewatcher.picker;

import info.lusito.mapeditor.service.filewatcher.WatchedFile;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;

/*
todo:
- relay keys from filter field to list, so list selection works when focus is on filter
- case sensitivity checkbox
- location textfield
 */
public class FilePicker {

    private static final int SPACING = 5;
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 350;

    private final JPanel container = new JPanel(new GridBagLayout());
    private final JTextField filter;
    private final JTextField location;
    private final JList<WatchedFileWrapper> list;
    private final DefaultListModel<WatchedFileWrapper> listModel = new DefaultListModel();
    private Collection<WatchedFile> files;
    private Dialog dialog;
    private boolean accepted;
    private String[] allowedExtensions;

    public FilePicker() {
        container.setBorder(BorderFactory.createEmptyBorder(SPACING, SPACING, SPACING, SPACING));
        final Dimension size = new Dimension(MIN_WIDTH, MIN_HEIGHT);
        container.setMinimumSize(size);
        container.setPreferredSize(size);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.gridwidth = 1;
        constraints.gridx = 0;

        addLabel(constraints, 0, "File Name (prefix, camel case: \"AA\" or \"AbcAb\", wildcards: \"?\" \"*\", exact match: end with space)", 0, SPACING);
        filter = addTextField(constraints, 1);
        filter.getDocument().addDocumentListener(new MyDocumentListener());
        addLabel(constraints, 2, "Matching Files:", SPACING, SPACING);

        addLabel(constraints, 4, "Location:", SPACING, SPACING);
        location = addTextField(constraints, 5);
        location.setEditable(false);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1;

        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setVisibleRowCount(-1);
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    accepted = true;
                    dialog.setVisible(false);
                }
            }
        });
        list.getSelectionModel().addListSelectionListener(this::selectionChanged);
        JScrollPane listScroller = new JScrollPane(list);

        add(listScroller, constraints, 3);
    }
    
    public void setAllowedExtension(String... extensions) {
        allowedExtensions = extensions;
    }

    private void selectionChanged(ListSelectionEvent e) {
        final List<WatchedFileWrapper> s = list.getSelectedValuesList();
        if (s.isEmpty()) {
            location.setText("");
        } else {
            location.setText(s.get(0).file.getAbsolutePath());
        }
    }

    private void addLabel(GridBagConstraints constraints, int row, String text, int topSpacing, int bottomSpacing) {
        final JLabel label = new JLabel(text);
        label.setBorder(BorderFactory.createEmptyBorder(topSpacing, 0, bottomSpacing, 0));
        add(label, constraints, row);
    }

    private JTextField addTextField(GridBagConstraints constraints, int row) {
        JTextField field = new JTextField();
        add(field, constraints, row);
        return field;
    }

    private void add(JComponent component, GridBagConstraints constraints, int row) {
        constraints.gridy = row;
        container.add(component, constraints);
    }

    private class DialogButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("OK")) {
                accepted = true;
            }
        }
    }

    private boolean createDialog(Object innerPane, String title) {
        final Object[] buttons = new Object[]{DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION};

        accepted = false;
        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                innerPane,
                title,
                true,
                buttons,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP,
                new DialogButtonListener());
        dialogDescriptor.setClosingOptions(buttons);

        dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setVisible(true);
        return accepted;
    }

    public List<WatchedFile> show(Collection<WatchedFile> files, String title) {
        this.files = files;
        applyFilter();
        if (createDialog(container, title)) {
            // return selected files
            final List<WatchedFileWrapper> selectedValuesList = list.getSelectedValuesList();
            ArrayList<WatchedFile> result = new ArrayList(selectedValuesList.size());
            for (WatchedFileWrapper wfw : selectedValuesList) {
                result.add(wfw.file);
            }
            return result;
        }
        return null;
    }

    private boolean isAllowedExtension(WatchedFile file) {
        if(allowedExtensions == null) {
            return true;
        }
        
        String ext = file.getFileobject().getExt();
        for (String allowedExtension : allowedExtensions) {
            if(allowedExtension.equalsIgnoreCase(ext))
                return true;
        }
        return false;
    }
    
    private void addFile(WatchedFile file, String match) {
        if(isAllowedExtension(file)) {
            listModel.addElement(new WatchedFileWrapper(file,
                    String.format("<html>%s <i>(%s)</i></html>",
                            match, file.getPath())));
        }
    }

    private void applyFilter() {
        String pattern = filter.getText();
        listModel.clear();
        if (pattern.isEmpty()) {
            for (WatchedFile file : files) {
                addFile(file, file.getFilename());
            }
        } else {
            FilenameMatcher matcher = new FilenameMatcher(pattern, false);
            for (WatchedFile file : files) {
                String filename = file.getFilename();
                String match = matcher.match(filename);
                if (match != null) {
                    addFile(file, match);
                }
            }
        }
    }

    private class MyDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            applyFilter();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            applyFilter();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            applyFilter();
        }
    }

    private class WatchedFileWrapper {

        final WatchedFile file;
        final String name;

        public WatchedFileWrapper(WatchedFile file, String name) {
            this.file = file;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
