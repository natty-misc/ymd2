package cz.tefek.ymd2.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConfigManager
{
    private static Config settings;

    static File settingsFile = new File("settings.cfg");

    public static void init()
    {
        settings = new Config();

        try
        {
            if (settingsFile.exists())
            {
                load();
            }
            
            save();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void load() throws IOException
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(settingsFile)))
        {
            var gson = new Gson();
            settings = gson.fromJson(reader, Config.class);
        }
    }

    public static void save() throws IOException
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(settingsFile)))
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
