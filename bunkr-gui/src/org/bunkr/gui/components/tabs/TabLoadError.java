package org.bunkr.gui.components.tabs;

import org.bunkr.core.exceptions.BaseBunkrException;

/**
 * Creator: benmeier
 * Created At: 2016-01-06
 */
public class TabLoadError extends BaseBunkrException
{
    public TabLoadError(Throwable cause, String message, Object... args)
    {
        super(cause, message, args);
    }

    public TabLoadError(Throwable e)
    {
        super(e);
    }

    public TabLoadError(String message, Object... args)
    {
        super(message, args);
    }
}
