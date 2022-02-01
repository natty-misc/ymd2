package cz.tefek.ymd2.backend;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

import cz.tefek.ymd2.backend.converter.FFmpegConverter;
import cz.tefek.ymd2.backend.downloader.BinaryDownloader;
import cz.tefek.ymd2.config.Config;
import cz.tefek.ymd2.config.property.audio.AudioFormat;
import cz.tefek.ymd2.interconnect.progress.ProgressStatus;
import cz.tefek.ymd2.interconnect.progress.RetrieveProgressWatcher;
import cz.tefek.ymd2.util.DirectoryUtil;
import cz.tefek.ymd2.util.NameSimplifier;
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

    public static WorkerBuilder fromConfig(Config config)
    {
        var wb = new WorkerBuilder();
        wb.worker.config = config.copy();
        return wb;
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
                var converter = new FFmpegConverter(this.config.general.separateAudioVideo);

                var metadata = this.videoData.getMetadata();

                var nameRaw = metadata.title();
                var name = this.config.general.simplifyName ? NameSimplifier.simplifyName(nameRaw) : NameSimplifier.sanitizeName(nameRaw);

                if (this.config.general.separateAudioVideo)
                {
                    if (this.config.audio.enabled)
                    {
                        var audio = videoData.getBestAudio();

                        if (audio == null)
                        {
                            this.watcher.setErrorText("""
                                ERROR: No accessible media resource has been found.
                                Please check if the video is publicly available.
                                In other cases please try again later.
                                If this problem persists, please contact me...
                                """);
                            this.watcher.setStatus(ProgressStatus.FAILED);

                            return;
                        }

                        if (this.config.audio.format == AudioFormat.HIGHEST_AVAILABLE_BITRATE)
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

                            converter.setAudio(audioFile, this.config.audio.format, this.config.audio.bitrate, metadata.loudness());
                        }
                    }
                }
                else
                {
                    var video = videoData.getBestVideo();

                    if (video == null)
                    {
                        this.watcher.setErrorText("""
                            ERROR: No accessible media resource has been found. Please check if the video is publicly available.
                            In other cases please try again later. If this problem persists, please contact me...
                            """);
                        this.watcher.setStatus(ProgressStatus.FAILED);

                        return;
                    }

                    var videoFile = downloadTemp(video);

                    if (videoFile == null)
                        return;

                    tempFiles.add(videoFile);

                    converter.setVideo(videoFile, this.config.video.container, this.config.video.codec);

                    if (this.config.audio.enabled)
                    {
                        var audio = videoData.getBestAudio();

                        if (audio == null)
                        {
                            this.watcher.setErrorText("""
                                ERROR: No accessible media resource has been found. Please check if the video is publicly available.
                                In other cases please try again later. If this problem persists, please contact me...
                                """);
                            this.watcher.setStatus(ProgressStatus.FAILED);

                            return;
                        }

                        var audioFile = downloadTemp(audio);

                        if (audioFile == null)
                            return;

                        tempFiles.add(audioFile);

                        converter.setAudio(audioFile, this.config.audio.format, this.config.audio.bitrate, metadata.loudness());
                    }
                }

                DirectoryUtil.ensureDirectoryExists(Config.outputDirectory);
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

            DirectoryUtil.ensureDirectoryExists(baseOutputFolder);

            var data = multimedia.getMediaData();

            var finalName = String.format("%s.%s", outputFilename, data.getContainer());

            this.watcher.setStatus(data.getType() == MultimediaType.AUDIO ? ProgressStatus.DOWNLOADING_AUDIO : ProgressStatus.DOWNLOADING_VIDEO);

            System.out.printf("Attempting to download %s from %s%n", data.getType(), multimedia.getUrl());

            var finalFile = Path.of(finalName);
            var downloader = new BinaryDownloader(finalFile, multimedia.getUrl(), this.watcher);

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

            DirectoryUtil.ensureDirectoryExists(baseTempFolder);

            var tempName = String.format("%s.temp", UUID.randomUUID());

            var data = multimedia.getMediaData();

            this.watcher.setStatus(data.getType() == MultimediaType.AUDIO ? ProgressStatus.DOWNLOADING_AUDIO : ProgressStatus.DOWNLOADING_VIDEO);

            System.out.printf("Attempting to download %s from %s%n", data.getType(), multimedia.getUrl());

            var tempFile = baseTempFolder.resolve(tempName);
            var downloader = new BinaryDownloader(tempFile, multimedia.getUrl(), this.watcher);

            var downloadSucceeded = downloader.download();

            if (!downloadSucceeded)
            {
                this.watcher.setStatus(ProgressStatus.DELETING_TEMP_FILES);
                Files.deleteIfExists(tempFile);

                this.watcher.setErrorText("Download failed. Please try again later or check if the video is accessible.\nIf this problem persists, contact me.");
                this.watcher.setStatus(ProgressStatus.FAILED);

                return null;
            }

            return tempFile.toAbsolutePath();
        }
    }
}
