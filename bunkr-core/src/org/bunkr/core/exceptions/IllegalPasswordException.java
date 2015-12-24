package org.bunkr.core.exceptions;

/**
 * Creator: benmeier
 * Created At: 2015-12-16
 */
public class IllegalPasswordException extends BaseBunkrException
{
    public IllegalPasswordException(Throwable e)
    {
        super(e);
    }

    public IllegalPasswordException(String message, Object... args)
    {
        super(message, args);
    }
}
