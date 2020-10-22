package cz.tefek.ymd2.gui;

import javafx.beans.property.SimpleStringProperty;

public class AudioCodecInfo
{
    private final SimpleStringProperty codecName;
    private final SimpleStringProperty recommendedBitrate;

    public AudioCodecInfo(String codecName, String recommendedBitrate)
    {
        this.codecName = new SimpleStringProperty(codecName);
        this.recommendedBitrate = new SimpleStringProperty(recommendedBitrate);
    }

    public String getCodecName()
    {
        return this.codecName.get();
    }

    public String getRecommendedBitrate()
    {
        return this.recommendedBitrate.get();
    }
}
