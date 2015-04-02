package com.hctord.green.util;

/**
 * Compares two objects. Meant to be used as a value-wise comparison.
 * @param <E> Type to compare.
 */
public interface Comparer<E> {
    public boolean compare(E a, E b);
}
