/**
 * Copyright (c) 2016 Bunkr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bunkr.core.inventory;

import java.util.HashSet;
import java.util.Set;

/**
 * Need some basic mime type functionality for files. This is specifically an issue for binary files imported into
 * the GUI since they cannot be editted or viewed, only exported.
 *
 * These mime types might not be the official recognized ones.
 *
 * Created At: 2016-01-02
 */
public class MediaType
{
    public static final String UNKNOWN = "unknown";
    public static final String TEXT = "text";
    public static final String IMAGE = "image";
    public static final String HTML = "html";

    public static final String[] TEXT_EXT = {
            "md", "markdown", "mdown",
            "txt", "text", "log",
            "ini", "cfg"
    };

    public static final String[] IMAGE_EXT = {
            "jpg", "jpeg",
            "png",
            "ico",
            "bmp",
    };

    public static final String[] HTML_EXT = {
            "html",
            "xhtml",
            "htm"
    };

    public static final Set<String> ALL_TYPES = new HashSet<>();
    static {
        ALL_TYPES.add(UNKNOWN);
        ALL_TYPES.add(IMAGE);
        ALL_TYPES.add(TEXT);
        ALL_TYPES.add(HTML);
    }

    public static final Set<String> OPENABLE_TYPES = new HashSet<>();
    static {
        OPENABLE_TYPES.add(TEXT);
        OPENABLE_TYPES.add(IMAGE);
        OPENABLE_TYPES.add(HTML);
    }

    public static String guess(String filename)
    {
        int i = filename.lastIndexOf('.');
        if (i > 0)
        {
            String ext = filename.substring(i + 1);
            for (String s : TEXT_EXT)
            {
                if (ext.equals(s)) return MediaType.TEXT;
            }
            for (String s : IMAGE_EXT)
            {
                if (ext.equals(s)) return MediaType.IMAGE;
            }
            for (String s : HTML_EXT)
            {
                if (ext.equals(s)) return MediaType.HTML;
            }
        }
        return MediaType.UNKNOWN;
    }
}
