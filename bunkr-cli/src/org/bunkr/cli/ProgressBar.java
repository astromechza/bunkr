package org.bunkr.cli;

import java.util.Arrays;

/**
 * Creator: benmeier
 * Created At: 2015-12-09
 */
public class ProgressBar
{
    private final boolean enabled;
    private final String title;
    private final long total;
    private final long minIters;
    private final int width;
    private long n;
    private long lastPrintN;

    public ProgressBar(int width, long total, String title, boolean enabled, long minIters)
    {
        this.enabled = enabled;
        this.title = title;
        this.total = total;
        this.minIters = minIters;
        this.width = width;
        this.n = 0;
        this.lastPrintN = 0;
    }

    public ProgressBar(int width, long total, String title, boolean enabled)
    {
        this(width, total, title, enabled, total / 100);
    }

    public ProgressBar(int width, long total, String title)
    {
        this(width, total, title, true, total / 100);
    }

    public void startFresh()
    {
        if (enabled)
        {
            System.out.print('\r');
            System.out.print(formatState(n, total, width, title));
        }
    }

    public void inc(long d)
    {
        tick(this.n + d);
    }

    public void tick(long n)
    {
        this.n = n;
        if (enabled)
        {
            long deltaIt = n - lastPrintN;
            if (deltaIt > minIters)
            {
                System.out.print('\r');
                System.out.print(formatState(n, total, width, title));
                lastPrintN = n;
            }
        }
    }

    public void finish()
    {
        if (enabled)
        {
            if (n > lastPrintN)
            {
                System.out.print('\r');
                System.out.println(formatState(n, total, width, title));
            }
        }
    }

    private static String formatState(long n, long total, int ncols, String prefix)
    {
        double frac = n / (double) total;
        double percentage = frac * 100;
        String left = (prefix != null) ? prefix : "";
        String right = String.format("%3.0f%%", percentage);
        ncols -= left.length();
        ncols -= right.length();
        if (ncols < 4)
        {
            return left + right;
        }
        else
        {
            ncols -= 2;
            int barWidth = (int) (frac * ncols);
            char[] bar = new char[barWidth];
            char[] space = new char[ncols - barWidth];
            Arrays.fill(bar, '=');
            Arrays.fill(space, ' ');
            return String.format("%s|%s%s|%s", left, new String(bar), new String(space), right);
        }
    }
}
