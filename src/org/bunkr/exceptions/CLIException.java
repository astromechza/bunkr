package org.bunkr.exceptions;

/**
 * Creator: benmeier
 * Created At: 2015-12-03
 *
 * Raise this exception when a known and expected Exception occurs while processing a CLI command. It should be formatted
 * and printed WITHOUT a stacktrace.
 */
public class CLIException extends BaseBunkrException
{
    public CLIException(Throwable e)
    {
        super(e);
    }

    public CLIException(String message, Object... args)
    {
        super(message, args);
    }
}
