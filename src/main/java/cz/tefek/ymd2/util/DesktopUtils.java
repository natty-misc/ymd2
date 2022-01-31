package cz.tefek.ymd2.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;

public class DesktopUtils
{
    public static void showInExplorer(Path path)
    {
        if (!Desktop.isDesktopSupported())
        {
            showError("Could not show the file", """
                An error has occured while opening your file browser.
                It is likely your file browser is not supported.
                """);

            return;
        }

        var desktop = Desktop.getDesktop();

        if (desktop.isSupported(Desktop.Action.BROWSE_FILE_DIR))
        {
            desktop.browseFileDirectory(path.toFile());
        }
        else if (desktop.isSupported(Desktop.Action.BROWSE))
        {
            try
            {
                desktop.browse(path.getParent().toUri());
            }
            catch (IOException e)
            {
                e.printStackTrace();
                showError("Could not show the file", """
                    An error has occured while opening your file browser.
                    It is likely your file browser is not supported.
                    """);
            }
        }
        else
        {
            showError("Could not show the file", "The file was moved or deleted.");
        }
    }

    public static void showError(String header, String text)
    {
        var alert = new Alert(Alert.AlertType.ERROR, text, ButtonType.OK);
        alert.setTitle(header);
        alert.showAndWait();
    }
}
