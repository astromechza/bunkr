package com.bunkr_beta.exceptions;

import com.bunkr_beta.exceptions.BaseBunkrException;

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

    public CLIException(String message, String... args)
    {
        super(message, args);
    }
}
