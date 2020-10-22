package cz.tefek.ymd2.config.property;

public enum ShowLegacy
{
    ON("Enable"),
    OFF("Disable");

    private final String human;

    ShowLegacy(String humanReadableName)
    {
        this.human = humanReadableName;
    }

    @Override
    public String toString()
    {
        return this.human;
    }
}