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
    private boolean unitIsBytes;

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
        this.unitIsBytes = false;
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
            System.out.print(formatStateWithRate(n, total, width, title, 0, this.unitIsBytes));
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
            System.out.print(formatStateWithRate(n, total, width, title, elapsed, this.unitIsBytes));
            lastPrintN = n;
        }
    }

    public void finish()
    {
        if (enabled && n >= lastPrintN)
        {
            long elapsed = System.currentTimeMillis() - this.startTime;
            System.out.print('\r');
            System.out.println(formatStateWithRate(n, total, width, title, elapsed, this.unitIsBytes));
        }
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void setUnitIsBytes(boolean unitIsBytes)
    {
        this.unitIsBytes = unitIsBytes;
    }

    private static String buildBarMiddle(long n, long total, int ncols, long elapsedMs, boolean unitIsBytes)
    {
        String middle = "";
        int characterBudget = ncols;
        characterBudget -= 4;

        String rateBar;
        if (unitIsBytes)
            rateBar = Formatters.formatBytes(Units.SECOND * n / elapsedMs);
        else
            rateBar = Formatters.formatPrettyInt(Units.SECOND * n / elapsedMs);
        rateBar += "/s";

        // do we have space to draw the proposed text
        if (characterBudget >= rateBar.length())
        {
            middle += rateBar;
            characterBudget -= rateBar.length();

            double remainingMilliseconds = (elapsedMs / (double)(n)) * (total - n);
            if (remainingMilliseconds > 0)
            {
                String remText = Formatters.formatPrettyElapsedShort(remainingMilliseconds);
                if (characterBudget >= remText.length() + 1) middle += " " + remText;
            }
            else
            {
                String elapText = Formatters.formatPrettyElapsed(elapsedMs);
                if (characterBudget >= elapText.length() + 1) middle += " " + elapText;
            }
        }
        return "[" + middle + "]";
    }


    private static String formatStateWithRate(long n, long total, int ncols, String prefix, long elapsedMs, boolean unitIsBytes)
    {
        // calculate percentage
        double frac = n / (double) total;
        double percentage = frac * 100;

        // create elements at the ends of the progress bar
        String left = (prefix != null) ? prefix : "";
        String right = String.format("%3.0f%%", percentage);

        // if we have used up all the characters we have to work with, return
        ncols -= left.length();
        ncols -= right.length();
        if (ncols < 4) return left + right;

        // account for | at end of bar
        ncols -= 2;

        // create array of bar characters
        int barWidth = (int) (frac * ncols);
        char[] bar = new char[ncols];
        Arrays.fill(bar, 0, barWidth, '=');
        Arrays.fill(bar, barWidth, bar.length, ' ');

        if (elapsedMs > 0 && n > 0)
        {
            String middle = buildBarMiddle(n, total, ncols, elapsedMs, unitIsBytes);
            int startOfRateSection = ncols / 2 - middle.length() / 2;
            System.arraycopy(middle.toCharArray(), 0, bar, startOfRateSection, middle.length());
        }
        return String.format("%s|%s|%s", left, new String(bar), right);
    }
}
