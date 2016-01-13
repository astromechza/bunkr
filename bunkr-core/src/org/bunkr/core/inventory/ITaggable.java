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
import java.util.regex.Pattern;

/**
 * Creator: benmeier
 * Created At: 2015-12-01
 */
public interface ITaggable
{
    Pattern VALID_CHARS_PATTERN = Pattern.compile("^[a-z][a-z0-9_\\-\\.]+[a-z0-9]$");

    // bulk methods
    void setTags(Set<String> tags);
    Set<String> getTags();

    default boolean addTag(String tag)
    {
        tag = tag.trim().toLowerCase();
        validateTag(tag);
        return getTags().add(tag);
    }

    default boolean hasTag(String tag)
    {
        return getTags().contains(tag);
    }

    default boolean removeTag(String tag)
    {
        return getTags().remove(tag);
    }

    default void setCheckTags(Set<String> tags)
    {
        Set<String> ctags = new HashSet<>();
        for (String tag : tags)
        {
            tag = tag.trim().toLowerCase();
            validateTag(tag);
            ctags.add(tag);
        }
        this.setTags(ctags);
    }

    default void validateTag(String tag)
    {
        tag = tag.trim().toLowerCase();
        if (tag.length() < 3)
            throw new IllegalArgumentException("Tag length must be at least 3 characters");
        if (!VALID_CHARS_PATTERN.matcher(tag).matches())
            throw new IllegalArgumentException("Tag " + tag + " does not match required regex: " + VALID_CHARS_PATTERN.pattern());
    }
}
