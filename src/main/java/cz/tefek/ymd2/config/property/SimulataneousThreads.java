package cz.tefek.ymd2.config.property;

public enum SimulataneousThreads
{
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    EIGHT("8"),
    TWELVE("12"),
    SIXTEEN("16");

    private final String human;

    SimulataneousThreads(String humanReadableName)
    {
        this.human = humanReadableName;
    }

    @Override
    public String toString()
    {
        return this.human;
    }
}