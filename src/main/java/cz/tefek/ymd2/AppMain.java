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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

import cz.tefek.pluto.io.logger.Logger;
import cz.tefek.pluto.io.logger.SmartSeverity;
import cz.tefek.ymd2.background.WorkerManager;
import cz.tefek.ymd2.config.Config;
import cz.tefek.ymd2.config.ConfigManager;

public class AppMain extends Application
{
    private static String appVersion;

    private static HostServices hostServices;

    private static <T> T loadFXML(String fxml) throws IOException
    {
        var resource = AppMain.class.getResource(fxml + ".fxml");
        return FXMLLoader.load(resource);
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

        ConfigManager.init();

        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");

        try
        {
            var baseAppPane = (AnchorPane) loadFXML("DownloaderBase");
            Scene scene = new Scene(baseAppPane, 650, 700);
            stage.setMinHeight(400);
            stage.setMinWidth(650);
            stage.setTitle("YouTube MultiDownloader v." + appVersion);
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
        var temp = Path.of(Config.tempDirectory);

        if (Files.isDirectory(temp))
        {
            Logger.logf(SmartSeverity.INFO, "Clearing %s ...%n", temp.toAbsolutePath());
            var files = new Streams.FailableStream<>(Files.list(temp));
            files.filter(file -> file.getFileName().endsWith(".temp")).forEach(Files::delete);
        }

        try
        {
            var logs = Path.of("logs/");

            if (Files.isDirectory(logs))
            {
                Files.list(logs).forEach(file -> {
                    try
                    {
                        var attributes = Files.readAttributes(file, BasicFileAttributes.class);
                        var createdDate = attributes.creationTime();

                        // Delete logs older than one day
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
