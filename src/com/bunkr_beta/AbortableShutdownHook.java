package com.bunkr_beta;

/**
 * Creator: benmeier
 * Created At: 2015-12-14
 */
public class AbortableShutdownHook extends Thread
{
    private boolean aborted = false;

    public void abort()
    {
        this.aborted = true;
    }

    public void innerRun()
    {
        throw new RuntimeException("Not Implemented");
    }


    @Override
    public void run()
    {
        if (! this.aborted) this.innerRun();
    }
}
