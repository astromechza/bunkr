package org.bunkr.core.utils;

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
    public static final long MEBIBYTE = KIBIBYTE * KIBIBYTE;
    public static final long GIBIBYTE = KIBIBYTE * MEBIBYTE;
    public static final long TEBIBYTE = KIBIBYTE * GIBIBYTE;

    public static final long KILO = 1000;
    public static final long MEGA = KILO * KILO;
    public static final long GIGA = KILO * MEGA;
    public static final long TERA = KILO * GIGA;

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

    public static String formatPrettyInt(long n)
    {
        if (n < KILO)
        {
            return String.format("%dB", n);
        }
        else if (n < MEGA)
        {
            return String.format("%.1fK", n / (double) KILO);
        }
        else if (n < GIGA)
        {
            return String.format("%.1fM", n / (double) MEGA);
        }
        else if (n < TERA)
        {
            return String.format("%.1fG", n / (double) GIGA);
        }
        else
        {
            return String.format("%.1fT", n / (double) TERA);
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

    public static String wrap(String input, int linewidth)
    {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder(input);
        int i = 0;
        while (i + linewidth < sb.length() && (i = sb.lastIndexOf(" ", i + linewidth)) != -1) {
            sb.replace(i, i + 1, "\n");
        }
        return sb.toString();
    }
}
