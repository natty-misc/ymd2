package cz.tefek.ymd2.background.downloader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.function.Consumer;

public class RBCW implements ReadableByteChannel
{
    private final Consumer<Long> delegate;
    private final ReadableByteChannel rbc;
    private long readSoFar;

    RBCW(ReadableByteChannel rbc, Consumer<Long> delegate)
    {
        this.delegate = delegate;
        this.rbc = rbc;
    }

    @Override
    public void close() throws IOException
    {
        this.rbc.close();
    }

    public long getReadSoFar()
    {
        return this.readSoFar;
    }

    @Override
    public boolean isOpen()
    {
        return this.rbc.isOpen();
    }

    @Override
    public int read(ByteBuffer buffer) throws IOException
    {
        int bytesRead;

        if ((bytesRead = this.rbc.read(buffer)) > 0)
        {
            this.readSoFar += bytesRead;
            this.delegate.accept(this.readSoFar);
        }

        return bytesRead;
    }
}
