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
