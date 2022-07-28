package org.r358.poolnetty.common.concurrent;

/**
 * A wrapper for the concept of an event with a value.
 */
public interface ValueEvent<V>
{
    void on(V value);
}
