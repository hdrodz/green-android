package com.hctord.green.document;

/**
 * Created by HéctorD on 11/9/2014.
 */
public final class LayerViewMode {
    private LayerViewMode() {}

    public static final int ALL_SELECTED = 0,
                            ONLY_ACTIVE = 1,
                            ACTIVE_AND_ABOVE = 2,
                            ACTIVE_AND_BELOW = 3,
                            ALL_BUT_ACTIVE = 4;
}
