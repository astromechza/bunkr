package com.bunkr_beta.exceptions;

/**
 * Creator: benmeier
 * Created At: 2015-12-06
 */
public class TraversalException extends BaseBunkrException
{
    public TraversalException(Throwable e)
    {
        super(e);
    }

    public TraversalException(String message, String... args)
    {
        super(message, args);
    }
}
