package cz.tefek.ymd2.background;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

import cz.tefek.ymd2.background.converter.FFmpegConverter;
import cz.tefek.ymd2.background.downloader.BinaryDownloader;
import cz.tefek.ymd2.background.progress.ProgressStatus;
import cz.tefek.ymd2.background.progress.RetrieveProgressWatcher;
import cz.tefek.ymd2.config.Config;
import cz.tefek.ymd2.config.property.SeparateStreams;
import cz.tefek.ymd2.config.property.audio.AudioBitrate;
import cz.tefek.ymd2.config.property.audio.AudioFormat;
import cz.tefek.ymd2.config.property.audio.DownloadAudio;
import cz.tefek.ymd2.config.property.video.Convert;
import cz.tefek.ymd2.config.property.video.DownloadVideo;
import cz.tefek.ymd2.config.property.video.VideoCodec;
import cz.tefek.ymd2.config.property.video.VideoContainer;
import cz.tefek.youtubetoolkit.YouTubeVideoData;
import cz.tefek.youtubetoolkit.multimedia.MultimediaType;
import cz.tefek.youtubetoolkit.multimedia.YouTubeMultimedia;

public final class WorkerBuilder
{
    private final Worker worker;

    private WorkerBuilder()
    {
        this.worker = new Worker();
    }

    public static WorkerBuilder basicMP3Worker()
    {
        var builder = new WorkerBuilder();
        builder.worker.config = new Config() {{
            this.general.separateAudioVideo = SeparateStreams.ON;

            this.video.enabled = DownloadVideo.OFF;

            this.audio.enabled = DownloadAudio.ON;
            this.audio.format = AudioFormat.MP3;
            this.audio.bitrate = AudioBitrate.KBPS256;
        }};

        return builder;
    }

    public static WorkerBuilder basicMP4Worker()
    {
        var builder = new WorkerBuilder();
        builder.worker.config = new Config() {{
            this.general.separateAudioVideo = SeparateStreams.OFF;

            this.video.enabled = DownloadVideo.ON;
            this.video.container = VideoContainer.MP4;
            this.video.codec = VideoCodec.H264;
            this.video.convert = Convert.ON;

            this.audio.enabled = DownloadAudio.ON;
            this.audio.format = AudioFormat.AAC;
            this.audio.bitrate = AudioBitrate.KBPS128;
        }};

        return builder;
    }

    public Worker build(YouTubeVideoData videoData, RetrieveProgressWatcher watcher)
    {
        this.worker.videoData = videoData;
        this.worker.watcher = watcher;
        return this.worker;
    }

    public static final class Worker
    {
        private YouTubeVideoData videoData;
        private Config config;
        private RetrieveProgressWatcher watcher;

        public void download() throws Exception
        {
            var tempFiles = new ArrayList<Path>();

            try
            {
                var converter = new FFmpegConverter(config.general.separateAudioVideo == SeparateStreams.ON);

                var metadata = this.videoData.getMetadata();
                var name = NameSimplifier.simplifyName(metadata.getTitle());

                if (config.general.separateAudioVideo == SeparateStreams.ON)
                {
                    if (config.audio.enabled == DownloadAudio.ON)
                    {
                        var audio = videoData.getBestAudio();

                        if (audio == null)
                        {
                            this.watcher.setErrorText("ERROR: No accessible media resource has been found. Please check if the video is publicly available.\nIn other cases please try again later. If this problem persists, please contact me...");
                            this.watcher.setStatus(ProgressStatus.FAILED);

                            return;
                        }

                        if (config.audio.format == AudioFormat.HIGHEST_AVAILABLE_BITRATE)
                        {
                            var outputAudio = downloadDirect(audio, name);

                            if (outputAudio == null)
                                return;

                            this.watcher.addOutputFile(outputAudio);
                        }
                        else
                        {
                            var audioFile = downloadTemp(audio);

                            if (audioFile == null)
                                return;

                            tempFiles.add(audioFile);

                            converter.setAudio(audioFile, this.config.audio.format, this.config.audio.bitrate, metadata.getLoudness());
                        }
                    }
                }
                else
                {
                    var video = videoData.getBestVideo();

                    if (video == null)
                    {
                        this.watcher.setErrorText("ERROR: No accessible media resource has been found. Please check if the video is publicly available.\nIn other cases please try again later. If this problem persists, please contact me...");
                        this.watcher.setStatus(ProgressStatus.FAILED);

                        return;
                    }

                    var videoFile = downloadTemp(video);

                    if (videoFile == null)
                        return;

                    tempFiles.add(videoFile);

                    converter.setVideo(videoFile, this.config.video.container, this.config.video.codec);

                    if (config.audio.enabled == DownloadAudio.ON)
                    {
                        var audio = videoData.getBestAudio();

                        if (audio == null)
                        {
                            this.watcher.setErrorText("ERROR: No accessible media resource has been found. Please check if the video is publicly available.\nIn other cases please try again later. If this problem persists, please contact me...");
                            this.watcher.setStatus(ProgressStatus.FAILED);

                            return;
                        }

                        var audioFile = downloadTemp(audio);

                        if (audioFile == null)
                            return;

                        tempFiles.add(audioFile);

                        converter.setAudio(audioFile, this.config.audio.format, this.config.audio.bitrate, metadata.getLoudness());
                    }
                }

                ensureDirectoryExists(Config.outputDirectory);
                converter.convert(this.watcher, Config.outputDirectory, name);

                this.watcher.setStatus(ProgressStatus.SUCCESS);
            }
            finally
            {
                var state = this.watcher.getStatus();

                this.watcher.setStatus(ProgressStatus.DELETING_TEMP_FILES);

                for (var tempFile : tempFiles)
                    Files.deleteIfExists(tempFile);

                this.watcher.setStatus(state);
            }
        }

        private Path downloadDirect(YouTubeMultimedia multimedia, String outputFilename) throws Exception
        {
            final var baseOutputFolder = Config.outputDirectory;

            ensureDirectoryExists(baseOutputFolder);

            var data = multimedia.getMediaData();

            var finalName = String.format("%s.%s", outputFilename, data.getContainer());

            this.watcher.setStatus(data.getType() == MultimediaType.AUDIO ? ProgressStatus.DOWNLOADING_AUDIO : ProgressStatus.DOWNLOADING_VIDEO);

            System.out.printf("Attempting to download %s from %s%n", data.getType(), multimedia.getUrl());

            var finalFile = Path.of(finalName);
            var downloader = new BinaryDownloader(finalFile.toString(), multimedia.getUrl(), this.watcher);

            var downloadSucceeded = downloader.download();

            if (!downloadSucceeded)
            {
                this.watcher.setErrorText("Download failed. Please try again later or check the video is accessible.\nIf this problem persists, contact me.");
                this.watcher.setStatus(ProgressStatus.FAILED);

                return null;
            }

            return finalFile.toAbsolutePath();
        }

        private Path downloadTemp(YouTubeMultimedia multimedia) throws Exception
        {
            final var baseTempFolder = Config.tempDirectory;

            ensureDirectoryExists(baseTempFolder);

            var tempName = String.format("%s.temp", UUID.randomUUID());

            var data = multimedia.getMediaData();

            this.watcher.setStatus(data.getType() == MultimediaType.AUDIO ? ProgressStatus.DOWNLOADING_AUDIO : ProgressStatus.DOWNLOADING_VIDEO);

            System.out.printf("Attempting to download %s from %s%n", data.getType(), multimedia.getUrl());

            var tempFile = Path.of(baseTempFolder, tempName);
            var downloader = new BinaryDownloader(tempFile.toString(), multimedia.getUrl(), this.watcher);

            var downloadSucceeded = downloader.download();

            if (!downloadSucceeded)
            {
                this.watcher.setStatus(ProgressStatus.DELETING_TEMP_FILES);
                Files.deleteIfExists(tempFile);

                this.watcher.setErrorText("Download failed. Please try again later or check the video is accessible.\nIf this problem persists, contact me.");
                this.watcher.setStatus(ProgressStatus.FAILED);

                return null;
            }

            return tempFile.toAbsolutePath();
        }

        private static void ensureDirectoryExists(String path) throws IOException
        {
            var dir = Path.of(path);

            if (!Files.isDirectory(dir))
            {
                System.out.println(String.format("Creating the %s directory.", dir.toAbsolutePath()));
                Files.createDirectories(dir);
            }
        }
    }
}
