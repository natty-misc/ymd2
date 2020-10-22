package cz.tefek.ymd2.config.property.audio;

public enum AudioFormat
{
    MP3("MP3"),
    AAC("MP4A AAC"),
    OPUS("Opus"),
    VORBIS("Ogg Vorbis"),
    FLAC("Lossless Flac"),
    HIGHEST_AVAILABLE_BITRATE("Best original");

    private final String human;

    AudioFormat(String humanReadableName)
    {
        this.human = humanReadableName;
    }

    @Override
    public String toString()
    {
        return this.human;
    }
}