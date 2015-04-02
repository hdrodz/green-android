package com.hctord.green.util;

/**
 * Interface for a function that will test a condition.
 */
public interface Predicate<A> {
    public boolean test(A arg);
}
