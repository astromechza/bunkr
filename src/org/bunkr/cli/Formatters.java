package org.bunkr.cli;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Creator: benmeier
 * Created At: 2015-12-09
 */
public class Formatters
{
    public static final long KIBIBYTE = 1024;
    public static final long MEBIBYTE = 1024 * KIBIBYTE;
    public static final long GIBIBYTE = 1024 * MEBIBYTE;
    public static final long TEBIBYTE = 1024 * GIBIBYTE;

    public static String formatBytes(long numBytes)
    {
        if (numBytes < KIBIBYTE)
        {
            return String.format("%dB", numBytes);
        }
        else if (numBytes < MEBIBYTE)
        {
            return String.format("%.1fKi", numBytes / (float) KIBIBYTE);
        }
        else if (numBytes < GIBIBYTE)
        {
            return String.format("%.1fMi", numBytes / (float) MEBIBYTE);
        }
        else if (numBytes < TEBIBYTE)
        {
            return String.format("%.1fGi", numBytes / (float) GIBIBYTE);
        }
        else
        {
            return String.format("%.1fTi", numBytes / (float) TEBIBYTE);
        }
    }

    public static String formateDate(long milliseconds, String format)
    {
        DateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(new Date(milliseconds));
    }

    public static String formatIso8601utc(long milliseconds)
    {
        return formateDate(milliseconds, "yyyy-MM-dd'T'HH:mm:ssZ");
    }

    public static String formatPrettyDate(long milliseconds)
    {
        return formateDate(milliseconds, "MMM dd HH:mm");
    }
}
