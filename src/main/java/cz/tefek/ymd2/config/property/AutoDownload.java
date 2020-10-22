package cz.tefek.ymd2.config.property;

public enum AutoDownload
{
    ON("Enable"),
    OFF("Disable");

    private final String human;

    AutoDownload(String humanReadableName)
    {
        this.human = humanReadableName;
    }

    @Override
    public String toString()
    {
        return this.human;
    }
}