package cz.tefek.ymd2.backend;

public class YMDException extends RuntimeException
{
    public enum EnumYMDExceptionType
    {
        TYPE_403("Could not find or access the media, please try a different quality setting."),
        TYPE_404("""
        Could not access the media.
        You either got temporarily blocked, or if this problem persists, YouTube changed something.
        """.stripLeading());

        private final String text;

        EnumYMDExceptionType(String text)
        {
            this.text = text;
        }

        public String getText()
        {
            return this.text;
        }
    }

    protected EnumYMDExceptionType type;

    public YMDException(EnumYMDExceptionType type)
    {
        super(type.getText());
        this.type = type;
    }

    public EnumYMDExceptionType getType()
    {
        return this.type;
    }
}
