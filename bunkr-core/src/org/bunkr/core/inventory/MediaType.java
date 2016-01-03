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
    public static final MediaType UNKNOWN = new MediaType("application", "unknown");
    public static final MediaType TEXT_PLAIN = new MediaType("text", "plain");
    public static final MediaType TEXT_MARKDOWN = new MediaType("text", "markdown");
    public static final MediaType TEXT_HTML = new MediaType("text", "html");
    public static final MediaType TEXT_CODE = new MediaType("text", "code");

    public static final Set<MediaType> ALL_TYPES = new HashSet<>();
    static {
        ALL_TYPES.add(UNKNOWN);
        ALL_TYPES.add(TEXT_PLAIN);
        ALL_TYPES.add(TEXT_MARKDOWN);
        ALL_TYPES.add(TEXT_HTML);
        ALL_TYPES.add(TEXT_CODE);
    }

    private final String type;
    private final String subtype;

    public MediaType(String type, String subtype)
    {
        this.type = type.toLowerCase();
        this.subtype = subtype.toLowerCase();
    }

    public MediaType(String type)
    {
        this.type = type;
        this.subtype = null;
    }

    @Override
    public String toString()
    {
        if (this.subtype != null && this.subtype.length() > 0) return String.format("%s/%s", type, subtype);
        return type;
    }

    public static MediaType parse(String s)
    {
        s = s.trim();
        if (! s.contains("/")) return new MediaType(s);
        String[] parts = s.split("/");
        return new MediaType(parts[0], parts[1]);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaType mediaType = (MediaType) o;

        if (! type.equals(mediaType.type)) return false;
        return !(subtype != null ? !subtype.equals(mediaType.subtype) : mediaType.subtype != null);
    }

    @Override
    public int hashCode()
    {
        int result = type.hashCode();
        result = 31 * result + (subtype != null ? subtype.hashCode() : 0);
        return result;
    }
}
