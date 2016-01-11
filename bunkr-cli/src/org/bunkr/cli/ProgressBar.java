package org.bunkr.cli;

import org.bunkr.core.utils.Formatters;

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
    private long startTime;

    public ProgressBar(int width, long total, String title, boolean enabled, long minIters)
    {
        this.enabled = enabled;
        this.title = title;
        this.total = total;
        this.minIters = minIters;
        this.width = width;
        this.n = 0;
        this.lastPrintN = 0;
        this.startTime = System.currentTimeMillis();
    }

    public ProgressBar(int width, long total, String title, boolean enabled)
    {
        this(width, total, title, enabled, total / 100);
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
        long deltaIt = n - lastPrintN;
        if (enabled && deltaIt > minIters)
        {
            long elapsed = System.currentTimeMillis() - this.startTime;
            System.out.print('\r');
            System.out.print(formatStateWithRate(n, total, width, title, elapsed));
            lastPrintN = n;
        }
    }

    public void finish()
    {
        if (enabled)
        {
            if (n >= lastPrintN)
            {
                long elapsed = System.currentTimeMillis() - this.startTime;
                System.out.print('\r');
                System.out.println(formatStateWithRate(n, total, width, title, elapsed));
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

    private static String formatStateWithRate(long n, long total, int ncols, String prefix, long elapsedMs)
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

            char[] bar = new char[ncols];
            Arrays.fill(bar, 0, barWidth, '=');
            Arrays.fill(bar, barWidth, bar.length, ' ');

            if (elapsedMs > 0 && ncols >= 12)
            {
                String rateBar = Formatters.formatPrettyInt(1000 * n / elapsedMs);
                int rateBarL = rateBar.length();
                int startOfRateSection = ncols / 2 - rateBarL / 2;
                bar[startOfRateSection - 1] = '[';
                System.arraycopy(rateBar.toCharArray(), 0, bar, startOfRateSection, rateBarL);
                bar[startOfRateSection + rateBarL] = '/';
                bar[startOfRateSection + rateBarL + 1] = 's';
                bar[startOfRateSection + rateBarL + 2] = ']';
            }
            return String.format("%s|%s|%s", left, new String(bar), right);
        }
    }
}
