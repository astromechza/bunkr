/**
 * Copyright (c) 2016 Bunkr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

    public static final long MILLISECOND = 1;
    public static final long SECOND = 1000 * MILLISECOND;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;

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

    public static String formatPrettyElapsed(double milliseconds)
    {
        if (milliseconds == 0) return "0";
        if (milliseconds < 0) return "Unknown";
        if (milliseconds > DAY) return String.format("%.1f Days", milliseconds / (double) DAY);
        if (milliseconds > HOUR) return String.format("%.1f Hours", milliseconds / (double) HOUR);
        if (milliseconds > MINUTE) return String.format("%.1f Minutes", milliseconds / (double) MINUTE);
        if (milliseconds > SECOND) return String.format("%.1f Seconds", milliseconds / (double) SECOND);
        return String.format("%d Milliseconds", (long) milliseconds);
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
