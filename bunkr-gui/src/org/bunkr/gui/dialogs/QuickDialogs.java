package org.bunkr.gui.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import org.bunkr.gui.Logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Creator: benmeier
 * Created At: 2015-12-27
 */
public class QuickDialogs
{
    public static void info(String title, String header, String format, Object... args)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(String.format(format, args));
        alert.showAndWait();
    }

    public static void info(String format, String... args)
    {
        info("Info", null, format, args);
    }

    public static boolean confirm(String title, String header, String format, Object... args)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(String.format(format, args));
        ButtonType positiveButton = new ButtonType("Yes");
        ButtonType negativeButton = new ButtonType("No");
        alert.getButtonTypes().setAll(positiveButton, negativeButton);
        Optional<ButtonType> result = alert.showAndWait();
        return result.get() == positiveButton;
    }

    public static boolean confirm(String format, Object... args)
    {
        return confirm("Input Required", null, format, args);
    }

    public static void exception(Throwable e)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception thrown");
        alert.setHeaderText(e.getClass().getName());
        alert.setContentText(e.getMessage());

        // Create stacktrace
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        // Log to stdout
        Logging.exception(e);

        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        alert.getDialogPane().setExpandableContent(textArea);
        alert.showAndWait();
    }

    public static String input(String content, String placeholder)
    {
        TextInputDialog dialog = new TextInputDialog(placeholder);
        dialog.setTitle("Input Required");
        dialog.setHeaderText(null);
        dialog.setContentText(content);

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent())
        {
            return result.get();
        }
        return null;
    }

    public static void error(String title, String message, Object... args)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(String.format(message, args));
        alert.showAndWait();
    }

    public static void error(String format, Object... args)
    {
        error("Error", format, args);
    }
}
