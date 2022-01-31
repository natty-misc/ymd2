package cz.tefek.ymd2.config.property.video;

public enum PreferredResolution
{
    LOWEST("Lowest available"),
    Q_240P("240p"),
    Q_360P("360p"),
    Q_480P("480p"),
    Q_720P("720p"),
    Q_1080P("1080p (Full HD)"),
    Q_1440P("1440p"),
    Q_2160P("2160p (4k Ultra HD)"),
    Q_2880P("2880p (5k)"),
    Q_4320P("4320p (8k)"),
    HIGHEST("Highest available");

    private final String human;

    PreferredResolution(String humanReadableName)
    {
        this.human = humanReadableName;
    }

    @Override
    public String toString()
    {
        return this.human;
    }

    public static PreferredResolution getDefault()
    {
        return Q_1080P;
    }
}