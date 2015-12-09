package com.bunkr_beta.cli;

/**
 * Creator: benmeier
 * Created At: 2015-12-09
 */
public class ProgressBar
{
    private int oldProgress = -100;
    private int newProgress = 0;
    private long total;
    private long target;
    private int innerWidth;

    public ProgressBar(int width, long target)
    {
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
        newProgress = (int) (this.innerWidth * (float) progress / target);
        if ((newProgress - oldProgress) >= 1 || newProgress == 100)
        {
            String bar = new String(new char[newProgress]).replace("\0", "=");
            System.out.print(String.format("Importing file: |%-" + innerWidth + "s|\r", bar));
            oldProgress = newProgress;
        }
    }

    public void finish()
    {
        String bar = new String(new char[innerWidth]).replace("\0", "=");
        System.out.print(String.format("Importing file: |%-" + innerWidth + "s|\r", bar));
        System.out.println();
    }
}
