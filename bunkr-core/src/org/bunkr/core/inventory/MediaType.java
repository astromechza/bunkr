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
    public static final String UNKNOWN = "unknown";
    public static final String TEXT = "text";
    public static final String IMAGE = "image";

    public static final Set<String> ALL_TYPES = new HashSet<>();
    static {
        ALL_TYPES.add(UNKNOWN);
        ALL_TYPES.add(IMAGE);
        ALL_TYPES.add(TEXT);
    }

    public static final Set<String> OPENABLE_TYPES = new HashSet<>();
    static {
        OPENABLE_TYPES.add(TEXT);
        OPENABLE_TYPES.add(IMAGE);
    }
}
