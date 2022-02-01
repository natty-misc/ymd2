package cz.tefek.ymd2.config;

import java.nio.file.Path;

import cz.tefek.ymd2.config.property.audio.AudioBitrate;
import cz.tefek.ymd2.config.property.audio.AudioFormat;
import cz.tefek.ymd2.config.property.video.PreferredResolution;
import cz.tefek.ymd2.config.property.video.VideoCodec;
import cz.tefek.ymd2.config.property.video.VideoContainer;

public class Config
{
    public static Path tempDirectory = Path.of("temp");
    public static Path outputDirectory = Path.of("out");

    public General general;
    public Audio audio;
    public Video video;

    public Config()
    {
        this.general = new General();
        this.audio = new Audio();
        this.video = new Video();
    }

    public static class General
    {
        private General()
        {

        }

        public int threadCount = 4;
        public boolean separateAudioVideo = false;
        public boolean simplifyName = false;

        private General copy()
        {
            var old = this;

            return new General() {{
                this.threadCount = old.threadCount;
                this.separateAudioVideo = old.separateAudioVideo;
                this.simplifyName = old.simplifyName;
            }};
        }
    }

    public static class Audio
    {
        private Audio()
        {

        }

        public boolean enabled = true;
        public AudioFormat format = AudioFormat.MP3;
        public AudioBitrate bitrate = AudioBitrate.KBPS192;

        private Audio copy()
        {
            var old = this;

            return new Audio() {{
                this.enabled = old.enabled;
                this.format = old.format;
                this.bitrate = old.bitrate;
            }};
        }
    }

    public static class Video
    {
        private Video()
        {

        }

        public boolean enabled = false;
        public PreferredResolution preferredResolution = PreferredResolution.Q_720P;
        public boolean convert = true;
        public VideoContainer container = VideoContainer.MP4;
        public VideoCodec codec = VideoCodec.H264;

        private Video copy()
        {
            var old = this;

            return new Video() {{
                this.enabled = old.enabled;
                this.preferredResolution = old.preferredResolution;
                this.convert = old.convert;
                this.container = old.container;
                this.codec = old.codec;
            }};
        }
    }

    public Config copy()
    {
        var old = this;

        return new Config() {{
            this.general = old.general.copy();
            this.audio = old.audio.copy();
            this.video = old.video.copy();
        }};
    }
}
