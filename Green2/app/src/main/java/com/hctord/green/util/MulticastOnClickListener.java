package com.hctord.green.util;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
* Utility class for having more than one OnClickListener on a view.
*/
public class MulticastOnClickListener implements View.OnClickListener {

    private List<View.OnClickListener> listeners = new ArrayList<View.OnClickListener>();

    public void addListener(View.OnClickListener listener) {
        listeners.add(listener);
    }

    public void removeListener(View.OnClickListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onClick(View view) {
        for (View.OnClickListener listener : listeners) {
            listener.onClick(view);
        }
    }
}
