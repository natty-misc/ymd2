package cz.tefek.ymd2.backend.downloader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import com.google.common.net.HttpHeaders;

import cz.tefek.ymd2.backend.YMDException;
import cz.tefek.ymd2.interconnect.progress.RetrieveProgressWatcher;
import cz.tefek.youtubetoolkit.config.Configuration;

public class BinaryDownloader
{
    private final RetrieveProgressWatcher progressReport;

    private final Path localPath;

    private final HttpClient httpClient;

    private final URI remoteURI;

    private static final int BUFFER_SIZE = 4 * 1024;
    private static final int CHUNK_SIZE = 96 * BUFFER_SIZE;

    public BinaryDownloader(Path localPath, String remoteURI, RetrieveProgressWatcher progressReport) throws URISyntaxException
    {
        this.remoteURI = new URI(remoteURI);
        this.localPath = localPath;
        this.progressReport = progressReport;

        var httpClientBuilder = HttpClient.newBuilder();
        httpClientBuilder.version(Version.HTTP_2);
        httpClientBuilder.followRedirects(Redirect.ALWAYS);
        httpClientBuilder.connectTimeout(Duration.ofSeconds(20));
        this.httpClient = httpClientBuilder.build();
    }

    public boolean download() throws IOException, InterruptedException, URISyntaxException
    {
        var contentLength = this.getContentLength();

        if (contentLength <= 0)
        {
            return false;
        }

        this.progressReport.setFileSize(contentLength);

        var buffer = new byte[BUFFER_SIZE];
        var readLength = 0;
        var bytesReadTotal = 0L;

        try (var fos = Files.newOutputStream(this.localPath))
        {
            while (bytesReadTotal < contentLength)
            {
                var downloadRequestBuilder = HttpRequest.newBuilder();
                downloadRequestBuilder.GET();
                downloadRequestBuilder.header("User-Agent", Configuration.USER_AGENT);
                var range = String.format("?range=%d-%d", bytesReadTotal, Math.min(bytesReadTotal + CHUNK_SIZE - 1, contentLength - 1));
                var newURI = new URI(this.remoteURI.toString() + range);
                downloadRequestBuilder.uri(newURI);
                var downloadRequest = downloadRequestBuilder.build();

                var response = this.httpClient.send(downloadRequest, BodyHandlers.buffering(BodyHandlers.ofInputStream(), BUFFER_SIZE));

                var responseHeaders = response.headers();

                var responseLength = responseHeaders.firstValueAsLong(HttpHeaders.CONTENT_LENGTH).orElse(-1);


                if (responseLength <= 0)
                {
                    System.out.println("Got invalid content length response for " + this.remoteURI);
                    System.out.println("Content length: " + responseLength);

                    throw new RuntimeException("Got invalid length response: " + contentLength);
                }

                var downloadInputStream = response.body();

                for (;;)
                {
                    readLength = downloadInputStream.read(buffer, 0, BUFFER_SIZE);

                    if (readLength <= 0)
                    {
                        break;
                    }

                    bytesReadTotal += readLength;
                    fos.write(buffer, 0, readLength);
                    this.progressReport.setBytesTransfered(bytesReadTotal);
                }
            }
        }

        if (contentLength != bytesReadTotal)
        {
            var err = String.format("File size mismatch! %d vs %d", contentLength, bytesReadTotal);
            System.out.printf("Error: %s%n", err);
            throw new RuntimeException(err);
        }

        return true;
    }

    private long getContentLength() throws IOException, InterruptedException
    {

        var downloadRequestBuilder = HttpRequest.newBuilder();
        downloadRequestBuilder.method("HEAD", BodyPublishers.noBody());
        downloadRequestBuilder.uri(this.remoteURI);
        var downloadRequest = downloadRequestBuilder.build();

        var response = this.httpClient.send(downloadRequest, BodyHandlers.discarding());

        if (response.statusCode() != HttpURLConnection.HTTP_OK)
        {
            System.out.println("Got invalid status code response for " + this.remoteURI);
            System.out.printf("Status code: %d%n", response.statusCode());

            switch (response.statusCode())
            {
                case HttpURLConnection.HTTP_NOT_FOUND -> throw new YMDException(YMDException.EnumYMDExceptionType.TYPE_403);
                case HttpURLConnection.HTTP_FORBIDDEN -> throw new YMDException(YMDException.EnumYMDExceptionType.TYPE_404);
                default -> throw new RuntimeException("Got invalid status code response, status code " + response.statusCode());
            }
        }

        var responseHeaders = response.headers();

        var contentLength = responseHeaders.firstValueAsLong(HttpHeaders.CONTENT_LENGTH).orElse(-1);

        if (contentLength <= 0)
        {
            System.out.println("Got invalid content length response for " + this.remoteURI);
            System.out.println("Content length: " + contentLength);

            throw new RuntimeException("Got invalid length response: " + contentLength);
        }

        return contentLength;
    }
}
