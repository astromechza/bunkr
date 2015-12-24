package org.bunkr.core.exceptions;

/**
 * Creator: benmeier
 * Created At: 2015-12-06
 */
public class BaseBunkrException extends Exception
{
    public BaseBunkrException(String message, Object... args)
    {
        super(String.format(message, args));
    }

    public BaseBunkrException(Throwable e)
    {
        super(e);
    }

    public BaseBunkrException(Throwable cause, String message, Object... args)
    {
        super(String.format(message, args), cause);
    }
}
