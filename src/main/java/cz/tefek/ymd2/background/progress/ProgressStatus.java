package cz.tefek.ymd2.background.progress;

import javafx.scene.paint.Color;

public enum ProgressStatus
{
    RETRIEVING_METADATA(Color.ORANGE),
    QUEUED(Color.DEEPSKYBLUE),
    DOWNLOADING_VIDEO(Color.DARKSLATEBLUE),
    DOWNLOADING_AUDIO(Color.STEELBLUE),
    CONVERTING_AUDIO(Color.DARKCYAN),
    CONVERTING_VIDEO(Color.OLIVE),
    DELETING_TEMP_FILES(Color.NAVY),
    FAILED(Color.RED),
    SUCCESS(Color.GREEN);

    private final Color color;

    ProgressStatus(Color color)
    {
        this.color = color;
    }

    public Color getColor()
    {
        return this.color;
    }
}
