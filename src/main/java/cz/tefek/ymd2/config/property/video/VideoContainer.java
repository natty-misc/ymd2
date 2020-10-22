package cz.tefek.ymd2.config.property.video;

public enum VideoContainer
{
    MKV("Matroska/MKV"),
    MP4("MP4"),
    WEBM("Matroska/WebM"),
    AVI("AVI");

    private final String human;

    VideoContainer(String humanReadableName)
    {
        this.human = humanReadableName;
    }

    @Override
    public String toString()
    {
        return this.human;
    }

    public static VideoContainer getDefault()
    {
        return MP4;
    }
}