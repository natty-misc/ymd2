package cz.tefek.ymd2;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.commons.lang3.stream.Streams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import cz.tefek.pluto.io.logger.Logger;
import cz.tefek.pluto.io.logger.SmartSeverity;
import cz.tefek.ymd2.backend.WorkerManager;
import cz.tefek.ymd2.config.Config;
import cz.tefek.ymd2.config.ConfigManager;

public class AppMain extends Application
{
    private static String appVersion;

    private static HostServices hostServices;

    public static <T> T loadFXML(String fxml)
    {
        return loadFXML(fxml, null);
    }

    public static <TC, CC> TC loadFXML(String fxml, Consumer<CC> controllerInitializer)
    {
        try
        {
            var resource = AppMain.class.getResource(fxml + ".fxml");
            assert resource != null;
            var fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(resource);
            var component = fxmlLoader.<TC>load();
            var controller = fxmlLoader.<CC>getController();
            if (controllerInitializer != null)
                controllerInitializer.accept(controller);
            return component;
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void start(Stage stage)
    {
        try
        {
            this.clearTemp();
        }
        catch (IOException e)
        {
            Logger.logf(SmartSeverity.WARNING, "Failed to clear the temp directory.");
            e.printStackTrace();
        }

        hostServices = this.getHostServices();

        try
        {
            ConfigManager.init();
        }
        catch (IOException e)
        {
            Logger.logf(SmartSeverity.WARNING, "Failed to load configurations.");
            throw new UncheckedIOException(e);
        }


        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");

        try
        {
            var baseAppPane = AppMain.<AnchorPane>loadFXML("DownloaderBase");
            Scene scene = new Scene(baseAppPane, 650, 700);
            stage.setMinHeight(400);
            stage.setMinWidth(650);
            stage.setTitle("YMD2 v." + appVersion);
            stage.setScene(scene);
            stage.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void clearTemp() throws IOException
    {
        var temp = Config.tempDirectory;

        if (Files.isDirectory(temp))
        {
            Logger.logf(SmartSeverity.INFO, "Clearing %s ...%n", temp.toAbsolutePath());

            try (var fileList = Files.list(temp))
            {
                var files = new Streams.FailableStream<>(fileList);
                files.filter(file -> file.getFileName().endsWith(".temp")).forEach(Files::delete);
            }
        }

        try
        {
            var logs = Path.of("logs/");

            if (Files.isDirectory(logs))
            {
                try (var logList = Files.list(logs))
                {
                    logList.forEach(file -> {
                        try
                        {
                            var attributes = Files.readAttributes(file, BasicFileAttributes.class);
                            var createdDate = attributes.creationTime();

                            // Delete logs older than one week
                            if (System.currentTimeMillis() - createdDate.toMillis() > TimeUnit.DAYS.toMillis(7))
                            {
                                Files.deleteIfExists(file);
                            }
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void stop()
    {
        WorkerManager.destroy();
    }

    public static String getAppVersion()
    {
        return appVersion;
    }

    public static HostServices getAppHostServices()
    {
        return hostServices;
    }

    private static void loadFont(String location)
    {
        Logger.logf(SmartSeverity.ADDED, "Loading font '%s'.%n", location);
        try (var is = AppMain.class.getResourceAsStream(String.format("/assets/%s.ttf", location)))
        {
            if (is == null)
            {
                Logger.logf(SmartSeverity.WARNING, "Failed to load font '%s'.%n", location);
                return;
            }

            Font.loadFonts(is, 10);
        }
        catch (IOException e)
        {
            Logger.logf(SmartSeverity.WARNING, "Failed to load font '%s'.%n", location);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException
    {
        Logger.setup();

        try (var is = AppMain.class.getResourceAsStream("/version"))
        {
            assert is != null;
            var reader = new BufferedReader(new InputStreamReader(is));

            appVersion = reader.readLine();
        }
        catch (IOException e)
        {
            Logger.log(SmartSeverity.ERROR, "Warning: Failed to retrieve the version number.");
            e.printStackTrace();
            appVersion = "???";
        }

        loadFont("OpenSans-Bold");
        loadFont("OpenSans-BoldItalic");
        loadFont("OpenSans-Italic");
        loadFont("OpenSans-Regular");

        launch(args);

        Logger.close();
    }
}
