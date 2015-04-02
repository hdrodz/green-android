package com.hctord.green.util;

/**
 * Simple delegate type.
 */
public interface Action<P> {
    public void run(P parameter);
}
