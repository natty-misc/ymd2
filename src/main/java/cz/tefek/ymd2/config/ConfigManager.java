package cz.tefek.ymd2.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConfigManager
{
    private static Config settings;

    static Path settingsFile = Path.of("settings.cfg");

    public static void init()
    {
        settings = new Config();

        try
        {
            if (Files.isRegularFile(settingsFile))
                load();
            
            save();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void load() throws IOException
    {
        try (BufferedReader reader = Files.newBufferedReader(settingsFile))
        {
            var gson = new Gson();
            settings = gson.fromJson(reader, Config.class);
        }
    }

    public static void save() throws IOException
    {
        try (BufferedWriter writer = Files.newBufferedWriter(settingsFile))
        {
            var gb = new GsonBuilder();
            gb.setPrettyPrinting();
            var gson = gb.create();
            gson.toJson(settings, writer);
        }
    }

    public static Config getSettings()
    {
        return settings;
    }

    public static Config copySettings()
    {
        return settings.copy();
    }
}
