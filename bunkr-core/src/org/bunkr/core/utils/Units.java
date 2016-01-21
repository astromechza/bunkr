package org.bunkr.core.utils;

/**
 * Creator: benmeier
 * Created At: 2016-01-21
 */
public class Units
{
    public static final long KIBIBYTE = 1024;
    public static final long MEBIBYTE = KIBIBYTE * KIBIBYTE;
    public static final long GIBIBYTE = KIBIBYTE * MEBIBYTE;
    public static final long TEBIBYTE = KIBIBYTE * GIBIBYTE;

    public static final long KILO = 1000;
    public static final long MEGA = KILO * KILO;
    public static final long GIGA = KILO * MEGA;
    public static final long TERA = KILO * GIGA;

    public static final int MILLISECOND = 1;
    public static final int SECOND = 1000 * MILLISECOND;
    public static final int MINUTE = 60 * SECOND;
    public static final int HOUR = 60 * MINUTE;
    public static final int DAY = 24 * HOUR;
}
