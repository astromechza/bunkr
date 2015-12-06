package com.bunkr_beta.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Creator: benmeier
 * Created At: 2015-12-06
 */
public interface IFFTraversalTarget
{
    @JsonIgnore
    default boolean isAFolder() { return false; }
    @JsonIgnore
    default boolean isAFile() { return false; }
    @JsonIgnore
    default boolean isRoot() { return false; }
}
