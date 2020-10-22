package cz.tefek.ymd2.config.property;

public enum SeparateStreams
{
    ON("Enable"),
    OFF("Disable");

    private final String human;

    SeparateStreams(String humanReadableName)
    {
        this.human = humanReadableName;
    }

    @Override
    public String toString()
    {
        return this.human;
    }
}