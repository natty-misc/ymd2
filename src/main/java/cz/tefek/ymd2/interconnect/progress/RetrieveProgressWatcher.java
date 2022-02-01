package cz.tefek.ymd2.interconnect.progress;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import cz.tefek.ymd2.backend.WorkerBuilder;
import cz.tefek.ymd2.backend.WorkerManager;
import cz.tefek.ymd2.interconnect.progress.ProgressStatus;
import cz.tefek.youtubetoolkit.YouTubeVideoData;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

public class RetrieveProgressWatcher implements Observable
{
    private ProgressStatus status = ProgressStatus.QUEUED;

    private WorkerBuilder config;
    private YouTubeVideoData videoData;

    private long bytesTransfered;
    private long fileSize;

    private long secondsConverted;

    private String errorText;

    private final List<Path> outputFiles;

    private final List<InvalidationListener> listeners;

    private String videoID;

    public RetrieveProgressWatcher()
    {
        this.listeners = new ArrayList<>();
        this.outputFiles = new ArrayList<>();
    }

    public synchronized void setConfig(WorkerBuilder config)
    {
        this.config = config;
    }

    public synchronized void restart()
    {
        WorkerManager.newJob(this.videoID, this, this.config);
    }

    public void setVideoID(String id)
    {
        this.videoID = id;
    }

    public String getVideoID()
    {
        return this.videoID;
    }

    public synchronized void setVideoData(YouTubeVideoData videoData)
    {
        this.videoData = videoData;
    }

    public synchronized long getBytesTransfered()
    {
        return this.bytesTransfered;
    }

    public synchronized void setBytesTransfered(long bytesTransfered)
    {
        this.bytesTransfered = bytesTransfered;

        this.invalidate();
    }

    public synchronized long getFileSize()
    {
        return this.fileSize;
    }

    public synchronized ProgressStatus getStatus()
    {
        return this.status;
    }

    public synchronized void setStatus(ProgressStatus status)
    {
        this.status = status;

        this.invalidate();
    }

    public synchronized void setSecondsConverted(long secondsConverted)
    {
        this.secondsConverted = secondsConverted;

        this.invalidate();
    }

    public synchronized long getSecondsConverted()
    {
        return this.secondsConverted;
    }

    public synchronized long getSecondsTotal()
    {
        return this.getVideoData().getMetadata().length();
    }

    public synchronized void setFileSize(long fileSize)
    {
        this.fileSize = fileSize;
    }

    public synchronized String getErrorText()
    {
        return this.errorText;
    }

    public synchronized void setErrorText(String errorText)
    {
        this.errorText = errorText;

        this.invalidate();
    }

    public synchronized void invalidate()
    {
        this.listeners.forEach(listener -> listener.invalidated(this));
    }

    public synchronized YouTubeVideoData getVideoData()
    {
        return this.videoData;
    }

    public synchronized void addOutputFile(Path outputFile)
    {
        this.outputFiles.add(outputFile);
    }

    public synchronized List<Path> getOutputFiles()
    {
        return List.copyOf(this.outputFiles);
    }

    @Override
    public void addListener(InvalidationListener listener)
    {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener)
    {
        this.listeners.remove(listener);
    }
}
