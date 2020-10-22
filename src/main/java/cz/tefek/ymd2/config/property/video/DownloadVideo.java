package cz.tefek.ymd2.config.property.video;

public enum DownloadVideo
{
    ON("Enable"),
    OFF("Disable");

    private final String human;

    DownloadVideo(String humanReadableName)
    {
        this.human = humanReadableName;
    }

    @Override
    public String toString()
    {
        return this.human;
    }

    public static DownloadVideo getDefault()
    {
        return OFF;
    }
}