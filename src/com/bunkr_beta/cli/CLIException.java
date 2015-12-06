package com.bunkr_beta.cli;

/**
 * Creator: benmeier
 * Created At: 2015-12-03
 *
 * Raise this exception when a known and expected Exception occurs while processing a CLI command. It will be formatted
 * and printed WITHOUT a stacktrace.
 */
public class CLIException extends Exception
{
    public CLIException(String message, String... args)
    {
        super(String.format(message, args));
    }

    public CLIException(Throwable e)
    {
        super(e);
    }
}
