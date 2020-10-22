package cz.tefek.ymd2.config.property.video;

public enum VideoCodec
{
    H264("AVC (x264)"),
    H265("HEVC (x265)"),
    VP8("VP8"),
    VP9("VP9"),
    AV1("AV1");

    private final String human;

    VideoCodec(String humanReadableName)
    {
        this.human = humanReadableName;
    }

    @Override
    public String toString()
    {
        return this.human;
    }

    public static VideoCodec getDefault()
    {
        return H264;
    }
}
