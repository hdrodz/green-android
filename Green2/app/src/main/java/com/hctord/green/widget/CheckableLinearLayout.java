package com.hctord.green.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

import com.hctord.green.R;

/**
 * Linear layout that can be checked. MAGICK!
 */
public class CheckableLinearLayout extends LinearLayout implements Checkable {
    private boolean checked = false;
    private int checkedColor;
    private Drawable uncheckedColor;

    private void init() {
        Resources resources = getResources();
        checkedColor = resources.getColor(R.color.accent);
        uncheckedColor = resources.getDrawable(R.drawable.button_transparent);
    }

    public CheckableLinearLayout(Context context) {
        super(context);
        init();
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Change the checked state of the view
     *
     * @param checked The new checked state
     */
    @Override
    public void setChecked(boolean checked) {
        this.checked = checked;
        if (checked)
            setBackgroundColor(checkedColor);
        else
            setBackground(uncheckedColor);
    }

    /**
     * @return The current checked state of the view
     */
    @Override
    public boolean isChecked() {
        return checked;
    }

    /**
     * Change the checked state of the view to the inverse of its current state
     */
    @Override
    public void toggle() {
        setChecked(!checked);
    }
}
