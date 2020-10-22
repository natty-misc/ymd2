package cz.tefek.ymd2.config.property.audio;

public enum AudioBitrate
{
    KBPS32("32kbps"),
    KBPS64("64kbps"),
    KBPS96("96kbps"),
    KBPS128("128kbps"),
    KBPS160("160kbps"),
    KBPS192("192kbps"),
    KBPS256("256kpbs"),
    KBPS320("320kpbs");

    private final String human;

    AudioBitrate(String humanReadableName)
    {
        this.human = humanReadableName;
    }

    @Override
    public String toString()
    {
        return this.human;
    }
}
