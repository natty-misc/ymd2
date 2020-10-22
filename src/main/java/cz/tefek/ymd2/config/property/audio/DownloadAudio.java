package cz.tefek.ymd2.config.property.audio;

public enum DownloadAudio
{
    ON("Enable"),
    OFF("Disable");

    private final String human;

    DownloadAudio(String humanReadableName)
    {
        this.human = humanReadableName;
    }

    @Override
    public String toString()
    {
        return this.human;
    }
}