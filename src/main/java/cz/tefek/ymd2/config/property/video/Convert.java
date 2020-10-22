package cz.tefek.ymd2.config.property.video;

public enum Convert
{
    ON("Enable"),
    OFF("Disable");

    private final String human;

    Convert(String humanReadableName)
    {
        this.human = humanReadableName;
    }

    @Override
    public String toString()
    {
        return this.human;
    }

    public static Convert getDefault()
    {
        return OFF;
    }
}