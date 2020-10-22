package cz.tefek.ymd2.config;

import cz.tefek.ymd2.config.property.AutoDownload;
import cz.tefek.ymd2.config.property.SeparateStreams;
import cz.tefek.ymd2.config.property.ShowLegacy;
import cz.tefek.ymd2.config.property.SimulataneousThreads;
import cz.tefek.ymd2.config.property.audio.AudioBitrate;
import cz.tefek.ymd2.config.property.audio.AudioFormat;
import cz.tefek.ymd2.config.property.audio.DownloadAudio;
import cz.tefek.ymd2.config.property.video.Convert;
import cz.tefek.ymd2.config.property.video.DownloadVideo;
import cz.tefek.ymd2.config.property.video.Framerate;
import cz.tefek.ymd2.config.property.video.PreferredResolution;
import cz.tefek.ymd2.config.property.video.UnavailableResolutionAction;
import cz.tefek.ymd2.config.property.video.VideoCodec;
import cz.tefek.ymd2.config.property.video.VideoContainer;

public class Config implements Cloneable
{
    public static String tempDirectory = "temp";
    public static String outputDirectory = "out";

    public General general = new General();
    public Audio audio = new Audio();
    public Video video = new Video();

    public static class General implements Cloneable
    {
        public SimulataneousThreads threadCount = SimulataneousThreads.FOUR;
        public AutoDownload autoDownloadOnAddition = AutoDownload.ON;
        public ShowLegacy showLegacyStreams = ShowLegacy.ON;
        public SeparateStreams separateAudioVideo = SeparateStreams.OFF;
    }

    public static class Audio implements Cloneable
    {
        public DownloadAudio enabled = DownloadAudio.ON;
        public AudioFormat format = AudioFormat.MP3;
        public AudioBitrate bitrate = AudioBitrate.KBPS192;
    }

    public static class Video implements Cloneable
    {
        public DownloadVideo enabled = DownloadVideo.OFF;
        public PreferredResolution preferredResolution = PreferredResolution.Q_720P;
        public Framerate framerate = Framerate.STANDARD;
        public UnavailableResolutionAction unavailableResolutionAction = UnavailableResolutionAction.BETTER_FIRST;
        public Convert convert = Convert.ON;
        public VideoContainer container = VideoContainer.MP4;
        public VideoCodec codec = VideoCodec.H264;
    }

    public Config copy()
    {
        try
        {
            return (Config) this.clone();
        }
        catch (CloneNotSupportedException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
