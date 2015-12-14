package com.bunkr_beta.cli;

/**
 * Creator: benmeier
 * Created At: 2015-12-09
 */
public class ProgressBar
{
    private final String title;

    private final long target;
    private long total;

    private final int innerWidth;
    private int currentWidth = -1;

    public ProgressBar(int width, long target, String title)
    {
        this.title = title;
        this.total = 0;
        this.target = target;
        this.innerWidth = width - 2;
    }

    public void inc(long d)
    {
        this.total += d;
        tick(this.total);
    }

    public void tick(long progress)
    {
        this.total = progress;
        double fraction = Math.min(this.total / (double) this.target, 1);

        int newWidth = (int) (fraction * this.innerWidth);
        if ((newWidth - currentWidth) >= 1 || newWidth >= this.innerWidth)
        {
            currentWidth = newWidth;
            String bar = new String(new char[currentWidth]).replace("\0", "=");
            System.out.print(String.format("%s|%-" + innerWidth + "s|\r", title, bar));
        }
    }

    public void finish()
    {
        String bar = new String(new char[innerWidth]).replace("\0", "=");
        System.out.print(String.format("%s|%-" + innerWidth + "s|\r", title, bar));
        System.out.println();
    }
}
