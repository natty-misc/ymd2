package cz.tefek.ymd2.config.property.video;

public enum UnavailableResolutionAction
{
    SKIP("Skip it"),
    BETTER_FIRST("Try better first, then worse"),
    WORSE_FIRST("Try worse first, then better"),
    JUST_BETTER("Try only better qualities");

    private final String human;

    UnavailableResolutionAction(String humanReadableName)
    {
        this.human = humanReadableName;
    }

    @Override
    public String toString()
    {
        return this.human;
    }

    public static UnavailableResolutionAction getDefault()
    {
        return BETTER_FIRST;
    }
}