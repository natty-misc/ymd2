package cz.tefek.ymd2.background.converter;

import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

import java.io.IOException;

import cz.tefek.ymd2.background.progress.ProgressStatus;
import cz.tefek.ymd2.background.progress.RetrieveProgressWatcher;
import cz.tefek.ymd2.config.property.audio.AudioBitrate;
import cz.tefek.ymd2.config.property.audio.AudioFormat;
import cz.tefek.ymd2.config.property.video.VideoCodec;
import cz.tefek.ymd2.config.property.video.VideoContainer;
import cz.tefek.youtubetoolkit.multimedia.MultimediaType;

public class FFmpegConverter
{
    private FFmpegBuilder videoOutputBuilder;
    private FFmpegBuilder audioOutputBuilder;

    private final boolean separateStreams;

    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;

    private final FFmpegExecutor executor;

    private int audioBitrate;

    private double loudness = 0.0;

    private String audioFormat;
    private String audioEncoder;

    private String videoFormat;
    private String videoCodec;

    public FFmpegConverter(boolean separateStreams) throws IOException
    {
        this.ffmpeg = new FFmpeg("ffmpeg");
        this.ffprobe = new FFprobe("ffprobe");

        this.executor = new FFmpegExecutor(this.ffmpeg, this.ffprobe);

        this.separateStreams = separateStreams;
    }

    private static int getBitrate(AudioBitrate outputBitrate)
    {
        switch (outputBitrate)
        {
            case KBPS32:
                return 32000;
            case KBPS64:
                return 64000;
            case KBPS96:
                return 96000;
            case KBPS128:
                return 128000;
            case KBPS160:
                return 160000;
            case KBPS192:
                return 192000;
            case KBPS256:
                return 256000;
            case KBPS320:
                return 320000;
            default:
                System.err.println("Unknown bitrate specified, defaulting to 128k.");
                return 128000;
        }
    }

    private static String getAudioFormat(AudioFormat format)
    {
        switch (format)
        {
            case AAC:
                return "aac";
            case FLAC:
                return "flac";
            case MP3:
                return "mp3";
            case OPUS:
                return "opus";
            case VORBIS:
                return "ogg";
            case HIGHEST_AVAILABLE_BITRATE:
                return null;
            default:
                System.err.println("Unknown audio format specified.");
                return null;
        }
    }

    private static String getAudioEncoder(AudioFormat format)
    {
        switch (format)
        {
            case AAC:
                return "aac";
            case FLAC:
                return "flac";
            case MP3:
                return "libmp3lame";
            case OPUS:
                return "libopus";
            case VORBIS:
                return "libvorbis";
            case HIGHEST_AVAILABLE_BITRATE:
                return null;
            default:
                System.err.println("Unknown audio format specified.");
                return null;
        }
    }

    public void setAudio(Path file, AudioFormat format, AudioBitrate outputBitrate, double loudness) throws IOException
    {
        if (this.audioOutputBuilder == null)
        {
            this.audioOutputBuilder = new FFmpegBuilder();

            if (!this.separateStreams)
            {
                this.videoOutputBuilder = this.audioOutputBuilder;
            }
        }

        FFmpegProbeResult in = this.ffprobe.probe(sanitizePath(file.toAbsolutePath()));

        this.loudness = loudness;

        this.audioBitrate = getBitrate(outputBitrate);

        this.audioFormat = getAudioFormat(format);

        this.audioEncoder = getAudioEncoder(format);

        this.audioOutputBuilder.addInput(in);
    }

    private static String getVideoContainer(VideoContainer videoContainer)
    {
        switch (videoContainer)
        {
            case AVI:
                return "avi";
            case MP4:
                return "mp4";
            case MKV:
                return "mkv";
            case WEBM:
                return "webm";
            default:
                System.err.println("Unknown video container specified.");
                return null;
        }
    }

    private static String getVideoCodec(VideoCodec codec)
    {
        switch (codec)
        {
            case H264:
                return "libx264"; // works with avi, mp4, mkv
            case H265:
                return "libx265"; // works with mp4, mkv
            case VP8:
                return "libvpx"; // works with avi, mkv, webm
            case VP9:
                return "libvpx-vp9"; // works with avi, mp4, mkv, webm
            case AV1:
                return "av1"; // works with mp4, mkv, webm

            default:
                System.err.println("Unknown video codec specified.");
                return null;
        }
    }

    public void setVideo(Path file, VideoContainer videoContainer, VideoCodec codec) throws IOException
    {
        if (this.videoOutputBuilder == null)
        {
            this.videoOutputBuilder = new FFmpegBuilder();

            if (!this.separateStreams)
            {
                this.audioOutputBuilder = this.videoOutputBuilder;
            }
        }

        this.videoFormat = getVideoContainer(videoContainer);

        this.videoCodec = getVideoCodec(codec);

        FFmpegProbeResult in = this.ffprobe.probe(sanitizePath(file.toAbsolutePath()));

        this.videoOutputBuilder.addInput(in);
    }

    public void convert(RetrieveProgressWatcher watcher, String destination, String name)
    {
        if (this.separateStreams)
        {
            if (this.audioOutputBuilder != null)
            {
                if (this.audioFormat == null)
                {
                    throw new IllegalStateException("Received audio that should not be converted, aborting.");
                }

                var outputFilename = String.format("%s.%s", name, this.audioFormat);
                var outputFile = Path.of(destination, outputFilename).toAbsolutePath();
                watcher.addOutputFile(outputFile);

                String normalization = String.format(Locale.ENGLISH, "volume=%.2fdB", -loudness);
                this.audioOutputBuilder = this.audioOutputBuilder
                        .setAudioFilter(normalization)
                        .addOutput(sanitizePath(outputFile))
                        .setAudioBitRate(this.audioBitrate)
                        .setAudioCodec(this.audioEncoder)
                        .done();

                this.convert(MultimediaType.AUDIO, this.audioOutputBuilder, watcher);
            }

            if (this.videoOutputBuilder != null)
            {
                var outputFilename = String.format("%s-video.%s", name, this.videoFormat);
                var outputFile = Path.of(destination, outputFilename).toAbsolutePath();
                watcher.addOutputFile(outputFile);

                this.videoOutputBuilder = this.videoOutputBuilder
                        .addOutput(sanitizePath(outputFile))
                        .setVideoCodec(this.videoCodec)
                        .done();

                this.convert(MultimediaType.VIDEO, this.videoOutputBuilder, watcher);
            }
        }
        else
        {
            if (this.videoOutputBuilder == null || this.videoCodec == null || this.videoFormat == null)
            {
                throw new IllegalStateException("Cannot convert, converter received empty video data.");
            }

            var outputFilename = String.format("%s.%s", name, this.videoFormat);
            var outputFile = Path.of(destination, outputFilename).toAbsolutePath();
            watcher.addOutputFile(outputFile);

            if (this.audioEncoder != null)
            {
                String normalization = String.format(Locale.ENGLISH, "volume=%.2fdB", -loudness);

                this.videoOutputBuilder = this.videoOutputBuilder
                        .addOutput(sanitizePath(outputFile))
                        .setAudioFilter(normalization)
                        .setVideoCodec(this.videoCodec)
                        .setAudioCodec(this.audioEncoder)
                        .done();
            }
            else
            {
                this.videoOutputBuilder = this.videoOutputBuilder
                        .addOutput(sanitizePath(outputFile))
                        .setVideoCodec(this.videoCodec).done();
            }

            this.convert(MultimediaType.VIDEO, this.videoOutputBuilder, watcher);
        }
    }

    private static String sanitizePath(Path path)
    {
        return String.format("%s", path);
    }

    private void convert(MultimediaType type, FFmpegBuilder commandLine, RetrieveProgressWatcher watcher)
    {
        final var nanosPerSecond = TimeUnit.SECONDS.toNanos(1);
        final var displayedType = type == MultimediaType.AUDIO ? ProgressStatus.CONVERTING_AUDIO
                : ProgressStatus.CONVERTING_VIDEO;

        watcher.setStatus(displayedType);

        var job = this.executor.createJob(commandLine, progress ->
                watcher.setSecondsConverted(progress.out_time_ns / nanosPerSecond));

        job.run();
    }
}
