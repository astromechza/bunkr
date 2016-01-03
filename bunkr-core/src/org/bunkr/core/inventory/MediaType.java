package org.bunkr.core.inventory;

import java.util.HashSet;
import java.util.Set;

/**
 * Need some basic mime type functionality for files. This is specifically an issue for binary files imported into
 * the GUI since they cannot be editted or viewed, only exported.
 *
 * These mime types might not be the official recognized ones.
 *
 * Creator: benmeier
 * Created At: 2016-01-02
 */
public class MediaType
{
    public static final String UNKNOWN = "application/unknown";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String TEXT_MARKDOWN = "text/markdown";
    public static final String TEXT_HTML = "text/html";
    public static final String TEXT_CODE = "text/code";

    public static final Set<String> ALL_TYPES = new HashSet<>();
    static {
        ALL_TYPES.add(UNKNOWN);
        ALL_TYPES.add(TEXT_PLAIN);
        ALL_TYPES.add(TEXT_MARKDOWN);
        ALL_TYPES.add(TEXT_HTML);
        ALL_TYPES.add(TEXT_CODE);
    }
}
