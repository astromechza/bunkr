package org.bunkr.core.exceptions;

import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-12-15
 */
public class IntegrityHashError extends IOException
{
    public IntegrityHashError(Throwable cause)
    {
        super(cause);
    }

    public IntegrityHashError(String message)
    {
        super(message);
    }

    public IntegrityHashError(String message, Throwable cause)
    {
        super(message, cause);
    }
}
