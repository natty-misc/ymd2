package cz.tefek.ymd2.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cz.tefek.ymd2.config.property.audio.AudioBitrate;
import cz.tefek.ymd2.config.property.audio.AudioFormat;
import cz.tefek.ymd2.config.property.video.PreferredResolution;
import cz.tefek.ymd2.config.property.video.VideoCodec;
import cz.tefek.ymd2.config.property.video.VideoContainer;
import cz.tefek.ymd2.util.DirectoryUtil;

public class ConfigManager
{
    private static List<Config> configs;
    private static final List<Config> builtInConfigs;
    private static List<Config> customConfigs;

    private static final Path SETTINGS_DIR = Path.of("settings");

    private static final Path settingsFile = SETTINGS_DIR.resolve("configurations.json");

    static
    {
        builtInConfigs = new ArrayList<>();

        var mp3Default = new Config("MP3 [built-in]") {{
            this.general.separateAudioVideo = true;

            this.general.simplifyName = true;

            this.video.enabled = false;

            this.audio.enabled = true;
            this.audio.format = AudioFormat.MP3;
            this.audio.bitrate = AudioBitrate.KBPS256;
        }};

        builtInConfigs.add(mp3Default);

        var mp4Default = new Config("MP4 [built-in]") {{
            this.general.separateAudioVideo = false;

            this.video.enabled = true;
            this.video.container = VideoContainer.MP4;
            this.video.codec = VideoCodec.H264;
            this.video.convert = true;
            this.video.preferredResolution = PreferredResolution.HIGHEST;

            this.audio.enabled = true;
            this.audio.format = AudioFormat.AAC;
            this.audio.bitrate = AudioBitrate.KBPS128;
        }};

        builtInConfigs.add(mp4Default);
    }

    public static void init() throws IOException
    {
        DirectoryUtil.ensureDirectoryExists(SETTINGS_DIR);

        try
        {
            configs = new ArrayList<>();
            configs.addAll(builtInConfigs);
            customConfigs = new ArrayList<>();

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
            var om = new ObjectMapper();
            customConfigs = om.readValue(reader, new TypeReference<>() {});
            configs.addAll(customConfigs);
        }
    }

    public static void save() throws IOException
    {
        try (BufferedWriter writer = Files.newBufferedWriter(settingsFile))
        {
            var om = new ObjectMapper().writerWithDefaultPrettyPrinter();
            om.writeValue(writer, customConfigs);
        }
    }

    public static List<Config> getConfigs()
    {
        return configs;
    }
}
