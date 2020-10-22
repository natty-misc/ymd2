package cz.tefek.ymd2.background;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class NameSimplifier
{
    private static final List<String> uselessInformationEnclosed;
    private static final List<String> uselessInformation;

    private static final String RX_QUALITY = "(" + "HD|HQ|4K|1080p|720p|high( |-)definition|high( |-)quality| live|extended|royalty( |-)free|free( |-)download|no( |-)copyright|download" + ")";
    private static final String RX_SOURCE = "(original|official)";
    private static final String RX_LENGTH = "(extended|extended version|long version|longer version|full|full version)";
    private static final String RX_SOURCE_LENGTH = "(" + RX_SOURCE + "|" + RX_LENGTH + ")";
    private static final String RX_QUALITY_SOURCE_LENGTH = "(" + RX_SOURCE + "|" + RX_LENGTH + "|" + RX_QUALITY + ")";

    private static final String RX_OST = "(" + "((video)?(-| )?game )?(soundtrack|OST)" + ")";
    private static final String RX_ITEM_STRONG = "(" + RX_OST + "|lyrics?" + ")";
    private static final String RX_ITEM_MEDIUM = "(" + RX_ITEM_STRONG + "|music|video( |-)?(clip)?" + ")";
    private static final String RX_ITEM_WEAK = "(album|mix|clip|audio|stream|rap|performance|" + RX_ITEM_MEDIUM + ")";

    static
    {
        uselessInformation = new ArrayList<>(List.of(
                ("((" + RX_ITEM_WEAK + "|" + RX_QUALITY_SOURCE_LENGTH + ") ?)+").repeat(3),
                "(" + RX_SOURCE_LENGTH + " )*(" + RX_ITEM_WEAK + " ?)+(" + RX_QUALITY_SOURCE_LENGTH + ")+",
                "(" + RX_SOURCE_LENGTH + " )+(" + RX_ITEM_WEAK + " ?)+(" + RX_QUALITY_SOURCE_LENGTH + ")*",

                ("((" + RX_ITEM_MEDIUM + "|" + RX_QUALITY_SOURCE_LENGTH + ") ?)+").repeat(2),

                "((" + RX_ITEM_STRONG + "|" + RX_QUALITY_SOURCE_LENGTH + ") ?)+",

                "(" + RX_QUALITY + " )+audio",
                "audio( " + RX_QUALITY + ")+",

                "song$",
                "-$",
                "^-",
                "o(ﬀi|ﬃ)cial",

                "ＰＶ",
                "Ａ?ＭＶ",
                "ＢＧＭ",

                "1080p",
                "720p",
                "4K",

                "⁴ᴷ",
                "ᴴᴰ",

                "/w ?video",
                "with video",

                "(royalty free|royalty-free) (music|audio|album)",

                "&?fmt=[0-9]+",

                "[♫♩]",
                "\uD83D\uDD0A",
                "\uD83D\uDD09",
                "\uD83D\uDD08"
        ));

        uselessInformationEnclosed = new ArrayList<>(List.of(
                "[\\w ]+? version",
                "[\\w ]+? release",
                "[\\w ]+? (official|original)( (lyrics?|music))?( (video|audio|soundtrack|OST))?",
                "[\\w ]+? album( mix)?",

                "download",
                "official",
                "music",
                "video",
                "original",
                "PV",
                "A?MV",
                "BGM",
                "HD",
                "HQ"
        ));

        uselessInformationEnclosed.addAll(0, uselessInformation);
    }

    public static String simplifyName(String input)
    {
        var simplified = input.strip();

        for (var pat : uselessInformationEnclosed)
        {
            for (var enclosings : List.of("\\{ *?%s *?\\}", "\\*+ *?%s *?\\*+   ", "\\[ *?%s *?\\]", "\\( *?%s *?\\)", "\\| *?%s *?", "【 *?%s *?】", "- *?%s *?"))
            {
                var finalPatternString = String.format(String.format(" *?%s", enclosings), pat);
                var pattern = Pattern.compile(finalPatternString, Pattern.CASE_INSENSITIVE);
                var matcher = pattern.matcher(simplified);
                simplified = matcher.replaceAll("").strip();
            }
        }

        for (var pat : uselessInformation)
        {
            var pattern = Pattern.compile(String.format(" *?%s", pat), Pattern.CASE_INSENSITIVE);
            var matcher = pattern.matcher(simplified);
            simplified = matcher.replaceAll("").strip();
        }

        return sanitizeName(simplified).strip();
    }

    public static String sanitizeName(String input)
    {
        return input.replaceAll("[:]", " ").replaceAll("[<>\"{}/|?*\\\\]", "");
    }
}
