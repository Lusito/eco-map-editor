package info.lusito.mapeditor.utils;

import java.awt.Dimension;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public class DialogUtil {
    
    public static boolean promptForm(SimpleForm.Entry[] entries, String title) {
        SimpleForm form = new SimpleForm(entries);
        int mt = NotifyDescriptor.PLAIN_MESSAGE;
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(form, title, NotifyDescriptor.OK_CANCEL_OPTION, mt);
        Object result = DialogDisplayer.getDefault().notify(d);
        return result == NotifyDescriptor.OK_OPTION;
    }

    public static String prompt(String title, String label, String defaultValue) {
        NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(label, title);
        d.setInputText(defaultValue);
        Object result = DialogDisplayer.getDefault().notify(d);
        if (result == NotifyDescriptor.OK_OPTION) {
            return d.getInputText();
        }
        return null;
    }

    public static String promptMultiline(String title, String defaultValue) {
        JTextArea area = new JTextArea(defaultValue);
        JScrollPane jp = new JScrollPane(area);
        jp.setPreferredSize(new Dimension(600, 250));
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(jp, NotifyDescriptor.OK_CANCEL_OPTION, Icon.PLAIN.type);
        d.setTitle(title);
        Object result = DialogDisplayer.getDefault().notify(d);
        if (result == NotifyDescriptor.OK_OPTION) {
            return area.getText();
        }
        return null;
    }

    public static boolean confirm(String title, String message, boolean yesNo) {
        final int style = yesNo ? NotifyDescriptor.YES_NO_OPTION: NotifyDescriptor.OK_CANCEL_OPTION;
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(message, title, style);
        Object result = DialogDisplayer.getDefault().notify(d);
        return result == NotifyDescriptor.OK_OPTION || result == NotifyDescriptor.YES_OPTION;
    }

    public static Result confirmYesNoCancel(String title, String message) {
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(message, title, NotifyDescriptor.YES_NO_CANCEL_OPTION);
        Object result = DialogDisplayer.getDefault().notify(d);
        if(result == NotifyDescriptor.YES_OPTION)
            return Result.YES;
        if(result == NotifyDescriptor.NO_OPTION)
            return Result.NO;
        return Result.CANCEL;
    }

    public static Result confirmSaveDiscardCancel(String title, String message) {
        String saveAnswer = "Save";
        String discardAnswer = "Discard";
        String cancelAnswer = "Cancel";
        String[] options = {saveAnswer, discardAnswer, cancelAnswer};  
        NotifyDescriptor nd = new NotifyDescriptor(message, title,
                NotifyDescriptor.YES_NO_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE,
                options, saveAnswer
        );
        Object result = DialogDisplayer.getDefault().notify(nd);
        if (result == saveAnswer) {
            return Result.SAVE;
        }
        if (result == discardAnswer) {
            return Result.DISCARD;
        }
        return Result.CANCEL;
    }

    public static void message(String message) {
        message(message, Icon.INFO);
    }

    public static void message(String message, Icon icon) {
        NotifyDescriptor d = new NotifyDescriptor.Message(message, icon.type);
        DialogDisplayer.getDefault().notify(d);
    }
    
    public static boolean custom(Object innerPane, boolean isModal, String title) {
        DialogDescriptor d = new DialogDescriptor(innerPane, title, isModal, null);
        Object result = DialogDisplayer.getDefault().notify(d);
        return result == DialogDescriptor.OK_OPTION;
    }

    public static void showException(String title, Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        JTextArea area = new JTextArea(sw.toString());
        JScrollPane jp = new JScrollPane(area);
        jp.setPreferredSize(new Dimension(600, 250));
        NotifyDescriptor d = new NotifyDescriptor.Message(jp, Icon.ERROR.type);
        d.setTitle(title);
        DialogDisplayer.getDefault().notify(d);
    }

    public static enum Icon {
        INFO(NotifyDescriptor.INFORMATION_MESSAGE),
        WARN(NotifyDescriptor.WARNING_MESSAGE),
        ERROR(NotifyDescriptor.ERROR_MESSAGE),
        QUESTION(NotifyDescriptor.QUESTION_MESSAGE),
        PLAIN(NotifyDescriptor.PLAIN_MESSAGE);
        
        public final int type;

        Icon(int type) {
            this.type = type;
        }
    }

    public static enum Result {
        YES,
        NO,
        SAVE,
        DISCARD,
        CANCEL
    }
}
