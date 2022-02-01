package cz.tefek.ymd2.backend;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cz.tefek.ymd2.interconnect.progress.RetrieveProgressWatcher;
import cz.tefek.ymd2.interconnect.progress.ProgressStatus;
import cz.tefek.youtubetoolkit.YouTubeRetriever;
import cz.tefek.youtubetoolkit.YouTubeVideoData;

public class WorkerManager
{
    public static final int taskSoftlimit = 1024;

    public static final int workerCount = 8;
    private static final ExecutorService workerPool = Executors.newWorkStealingPool(workerCount);
    private static final ExecutorService retrieverPool = Executors.newCachedThreadPool();

    public static void newJob(String id, RetrieveProgressWatcher watcher, WorkerBuilder workerBuilder)
    {
        watcher.setVideoID(id);

        retrieverPool.submit(() ->
        {
            var videoData = getVideoData(id, watcher);

            if (videoData == null)
            {
                return;
            }

            watcher.setVideoData(videoData);
            watcher.setConfig(workerBuilder);
            watcher.setStatus(ProgressStatus.QUEUED);

            workerPool.submit(() ->
            {
                var worker = workerBuilder.build(videoData, watcher);

                try
                {
                    worker.download();
                }
                catch (InterruptedException e)
                {
                    watcher.setErrorText("Aborted");
                    watcher.setStatus(ProgressStatus.FAILED);
                }
                catch (Exception e)
                {
                    watcher.setErrorText("Error: " + e.getMessage());
                    watcher.setStatus(ProgressStatus.FAILED);
                    e.printStackTrace();
                }
            });
        });
    }

    private static YouTubeVideoData getVideoData(String id, RetrieveProgressWatcher watcher)
    {
        watcher.setStatus(ProgressStatus.RETRIEVING_METADATA);

        try
        {
            return YouTubeRetriever.retrieveVideoData(id);
        }
        catch (Exception e)
        {
            watcher.setErrorText("Error retrieving media data: " + e);
            watcher.setStatus(ProgressStatus.FAILED);
            e.printStackTrace();
        }

        return null;
    }

    public static void destroy()
    {
        workerPool.shutdownNow();
        retrieverPool.shutdownNow();
    }
}
