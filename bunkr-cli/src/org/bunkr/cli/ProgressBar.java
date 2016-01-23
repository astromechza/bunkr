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

package org.bunkr.cli;

import org.bunkr.core.utils.Formatters;
import org.bunkr.core.utils.Units;

import java.util.Arrays;

/**
 * Created At: 2015-12-09
 */
public class ProgressBar
{
    private final String title;
    private final long total;
    private final long minIters;
    private final long startTime;
    private final int width;
    private boolean enabled;
    private long n;
    private long lastPrintN;

    public ProgressBar(int width, long total, String title, long minIters)
    {
        this.enabled = true;
        this.title = title;
        this.total = total;
        this.minIters = minIters;
        this.width = width;
        this.n = 0;
        this.lastPrintN = 0;
        this.startTime = System.currentTimeMillis();
    }

    public ProgressBar(int width, long total, String title)
    {
        this(width, total, title, total / 100);
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

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
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
                String rateBar = Formatters.formatPrettyInt(Units.SECOND * n / elapsedMs);
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
