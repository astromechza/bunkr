package org.bunkr.core.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * We need a basic way of sending stdout logging messages to the console. Instead of using a full-blown logging
 * library we will just use a basic static class since we only need very basic functionality.
 *
 * Remember that logging informat such as file names, passwords, encryption keys, etc.. classifies as information
 * leakage, so logging should only be enabled when the user is aware of this issue.
 *
 * Creator: benmeier
 * Created At: 2016-01-02
 */
public class Logging
{
    private static boolean enabled = false;

    public static void log(String prefix, String format, Object... args)
    {
        if (enabled)
        {
            String datetime = Formatters.formatIso8601utc(System.currentTimeMillis());
            System.out.println(String.format("%s [%s] %s", datetime, prefix, String.format(format, args)));
        }
    }

    public static void debug(String format, Object... args)
    {
        log("DEBUG", format, args);
    }

    public static void info(String format, Object... args)
    {
        log("INFO", format, args);
    }

    public static void warn(String format, Object... args)
    {
        log("WARN", format, args);
    }

    public static void error(String format, Object... args)
    {
        log("ERROR", format, args);
    }

    public static void exception(Throwable e)
    {
        // Create stacktrace
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        log("EXCEPTION", "{%s} %s: %s", e.getClass().getCanonicalName(), e.getLocalizedMessage(), sw.toString());
    }

    public static boolean isEnabled()
    {
        return enabled;
    }

    public static void setEnabled(boolean enabled)
    {
        Logging.enabled = enabled;
    }
}
