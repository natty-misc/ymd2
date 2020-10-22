package cz.tefek.ymd2.config.property.video;

public enum Framerate
{
    STANDARD("Standard"),
    BEST("Best available");

    private final String human;

    Framerate(String humanReadableName)
    {
        this.human = humanReadableName;
    }

    @Override
    public String toString()
    {
        return this.human;
    }

    public static Framerate getDefault()
    {
        return STANDARD;
    }
}