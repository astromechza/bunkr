package com.bunkr_beta.cli;

/**
 * Creator: benmeier
 * Created At: 2015-12-09
 */
public class ProgressBar
{
    private final String title;
    private final long target;
    private final int innerWidth;

    private int stepProgress = -100;
    private long total;

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
        int newProgress = (int) (this.innerWidth * (float) progress / target);
        if ((newProgress - stepProgress) >= 1 || newProgress == 100)
        {
            String bar = new String(new char[newProgress]).replace("\0", "=");
            System.out.print(String.format("%s|%-" + innerWidth + "s|\r", title, bar));
            stepProgress = newProgress;
        }
    }

    public void finish()
    {
        String bar = new String(new char[innerWidth]).replace("\0", "=");
        System.out.print(String.format("%s|%-" + innerWidth + "s|\r", title, bar));
        System.out.println();
    }
}
