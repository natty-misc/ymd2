package cz.tefek.ymd2.util;

import java.util.Locale;

public class BinaryUnitUtil
{

    static final String[] units = { "B", "kiB", "MiB", "GiB", "TiB" };

    public static String formatAsBinaryBytes(long bb)
    {

        double bbd = bb;
        byte bnu = 0;

        while (bbd > 1024)
        {
            bbd /= 1024.0;
            bnu++;
        }

        return String.format(Locale.ENGLISH, "%.2f", bbd) + units[bnu];
    }
}
