package com.hctord.green.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Checkable;

import com.hctord.green.R;
import com.hctord.green.util.MulticastOnClickListener;

/**
 * Color view i guess
 */
public class CheckableColorView
        extends View
        implements
            Checkable,
            View.OnClickListener {

    private MulticastOnClickListener multicastListener;
    private OnClickListener onClickListener;
    private boolean checked = false;
    private int color;
    private int checkedColor;
    private int uncheckedColor;
    //private StateListDrawable foreground;
    private Paint colorBorderPaint;
    private Paint colorFillPaint;
    private float padding;

    public CheckableColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        multicastListener = new MulticastOnClickListener();
        multicastListener.addListener(this);

        //super.setOnClickListener(multicastListener);

        Resources res = context.getResources();
        checkedColor = res.getColor(R.color.accent);
        uncheckedColor = 0;

        //foreground = (StateListDrawable) res.getDrawable(R.drawable.checkable_color_view_overlay);

        padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, res.getDisplayMetrics());

        colorBorderPaint = new Paint();
        colorBorderPaint.setStrokeWidth(1);
        colorBorderPaint.setStyle(Paint.Style.STROKE);
        colorBorderPaint.setColor(Color.BLACK);

        TypedArray arr = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CheckableColorView,
                0, 0);
        try {
            color = arr.getColor(R.styleable.CheckableColorView_previewColor, 0xFF008000);
            checked = arr.getBoolean(R.styleable.CheckableColorView_checked, false);
        }
        finally {
            arr.recycle();
        }

        colorFillPaint = new Paint();
        colorFillPaint.setStyle(Paint.Style.FILL);
        colorFillPaint.setColor(color);
    }

    public void setColor(int color) {
        this.color = color;
        colorFillPaint.setColor(color);
    }

    public int getColor() {
        return color;
    }

    /*@Override
    public void setOnClickListener(OnClickListener listener) {
        if (onClickListener != null)
            multicastListener.removeListener(onClickListener);
        multicastListener.addListener(onClickListener = listener);
    }*/

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(checked ? checkedColor : uncheckedColor);

        canvas.drawRect(padding, padding, getWidth() - padding, getHeight() - padding, colorFillPaint);
        canvas.drawRect(padding, padding, getWidth() - padding, getHeight() - padding, colorBorderPaint);

        //foreground.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                //foreground.setState(new int[] {android.R.attr.state_pressed});
                break;
            case MotionEvent.ACTION_UP:
                //foreground.setState(new int[] {-android.R.attr.state_pressed});
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void setChecked(boolean b) {
        checked = b;
        invalidate();
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        setChecked(!checked);
    }

    @Override
    public void onClick(View view) {
        toggle();
    }
}
