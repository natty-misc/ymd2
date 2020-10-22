package cz.tefek.ymd2.input;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidator
{
    public static String findVideoID(String url)
    {
        Matcher vidIdMatcher = Pattern.compile("^.*(?:(?:youtu\\.be/|v/|vi/|u/\\w/|embed/)|(?:(?:watch)?\\?v(?:i)?=|&v(?:i)?=))([^#&?]*).*").matcher(url);

        while (vidIdMatcher.find())
        {
            var vidID = vidIdMatcher.group(1);

            if (vidID.length() != 11)
            {
                continue;
            }

            return vidID;
        }

        return null;
    }
}
