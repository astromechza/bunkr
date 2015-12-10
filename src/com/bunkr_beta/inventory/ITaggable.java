package com.bunkr_beta.inventory;

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
        if (tag.length() < 3)
            throw new IllegalArgumentException("Tag length must be at least 3 characters");
        if (!VALID_CHARS_PATTERN.matcher(tag).matches())
            throw new IllegalArgumentException("Tag does not match required regex: " + VALID_CHARS_PATTERN.pattern());
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
            if (tag.length() < 3)
                throw new IllegalArgumentException("Tag length must be at least 3 characters");
            if (!VALID_CHARS_PATTERN.matcher(tag).matches())
                throw new IllegalArgumentException("Tag " + tag + " does not match required regex: " + VALID_CHARS_PATTERN.pattern());
            ctags.add(tag);
        }
        this.setTags(ctags);
    }

}
