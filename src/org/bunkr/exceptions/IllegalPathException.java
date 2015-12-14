package org.bunkr.exceptions;

/**
 * Creator: benmeier
 * Created At: 2015-12-06
 */
public class IllegalPathException extends IllegalArgumentException
{
    public IllegalPathException(String message, String... args)
    {
        super(String.format(message, args));
    }
}
