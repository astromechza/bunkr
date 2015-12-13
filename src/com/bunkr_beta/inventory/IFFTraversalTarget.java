package com.bunkr_beta.inventory;

/**
 * Creator: benmeier
 * Created At: 2015-12-06
 */
public interface IFFTraversalTarget
{
    default boolean isAFolder() { return false; }
    default boolean isAFile() { return false; }
    default boolean isRoot() { return false; }
}
