package com.bunkr_beta.cli;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Creator: benmeier
 * Created At: 2015-12-09
 */
public class Formatters
{
    public static final long KILOBYTE = 1024;
    public static final long MEGABYTE = 1024 * KILOBYTE;
    public static final long GIGABYTE = 1024 * MEGABYTE;
    public static final long TERABYTE = 1024 * GIGABYTE;

    public static String formatBytes(long numBytes)
    {
        if (numBytes < KILOBYTE)
        {
            return String.format("%dB", numBytes);
        }
        else if (numBytes < MEGABYTE)
        {
            return String.format("%.1fK", numBytes / (float)KILOBYTE);
        }
        else if (numBytes < GIGABYTE)
        {
            return String.format("%.1fM", numBytes / (float)MEGABYTE);
        }
        else if (numBytes < TERABYTE)
        {
            return String.format("%.1fG", numBytes / (float)GIGABYTE);
        }
        else
        {
            return String.format("%.1fT", numBytes / (float)TERABYTE);
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
