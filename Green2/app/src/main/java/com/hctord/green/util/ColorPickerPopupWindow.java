package com.hctord.green.util;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hctord.green.R;

/**
 *
 */
public class ColorPickerPopupWindow extends PopupWindow {

    private int color = 0xFF808080;
    private OnColorChangedListener listener;
    private View root;

    private TextView previewText,
            a, r, g, b;
    private SeekBar as, rs, gs, bs;
    private View preview;

    private SeekBar.OnSeekBarChangeListener listener_a = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            color = ((progress & 0xFF) << 24) | (color & 0x00FFFFFF);
            a.setText(Integer.toString(progress));
            updateColorPreviews();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }, listener_r = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            color = ((progress & 0xFF) << 16) | (color & 0xFF00FFFF);
            r.setText(Integer.toString(progress));
            updateColorPreviews();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }, listener_g = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            color = ((progress & 0xFF) << 8) | (color & 0xFFFF00FF);
            g.setText(Integer.toString(progress));
            updateColorPreviews();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }, listener_b = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            color = (progress & 0xFF) | (color & 0xFFFFFF00);
            b.setText(Integer.toString(progress));
            updateColorPreviews();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void updateColorPreviews() {
        preview.setBackgroundColor(color);
        preview.invalidate();
        if (previewText != null)
            previewText.setText('#' + Integer.toHexString(color).toUpperCase());
    }

    private void setup() {
        preview = root.findViewById(R.id.preview);
        previewText = (TextView)root.findViewById(R.id.preview_text);

        a = (TextView)root.findViewById(R.id.text_a);
        r = (TextView)root.findViewById(R.id.text_r);
        g = (TextView)root.findViewById(R.id.text_g);
        b = (TextView)root.findViewById(R.id.text_b);

        as = (SeekBar)root.findViewById(R.id.slide_a);
        rs = (SeekBar)root.findViewById(R.id.slide_r);
        gs = (SeekBar)root.findViewById(R.id.slide_g);
        bs = (SeekBar)root.findViewById(R.id.slide_b);

        as.setOnSeekBarChangeListener(listener_a);
        rs.setOnSeekBarChangeListener(listener_r);
        gs.setOnSeekBarChangeListener(listener_g);
        bs.setOnSeekBarChangeListener(listener_b);

        Button ok, cancel;

        ok = (Button)root.findViewById(R.id.ok);
        cancel = (Button)root.findViewById(R.id.cancel);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onColorChanged(color);
                dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    public ColorPickerPopupWindow(Context context, ViewGroup viewGroup) {

        root = LayoutInflater.from(context)
                .inflate(R.layout.activity_color_picker, viewGroup, false);
        Resources res = context.getResources();
        setBackgroundDrawable(res.getDrawable(android.R.drawable.dialog_holo_light_frame));
        setFocusable(true);
        setWidth((int) res.getDimension(R.dimen.color_picker_width));
        setHeight((int) res.getDimension(R.dimen.color_picker_height));
        setContentView(root);

        setup();
    }

    public void setColor(int color) {
        this.color = color;
        int ai, ri, gi, bi;
        ai = color & 0xFF000000;
        ri = color & 0x00FF0000;
        gi = color & 0x0000FF00;
        bi = color & 0x000000FF;

        ai >>= 24;
        ai &= 0x000000FF; // get rid of prepending FF's
        ri >>= 16;
        gi >>= 8;

        a.setText(Integer.toString(ai));
        r.setText(Integer.toString(ri));
        g.setText(Integer.toString(gi));
        b.setText(Integer.toString(bi));

        as.setProgress(ai);
        rs.setProgress(ri);
        gs.setProgress(gi);
        bs.setProgress(bi);

        updateColorPreviews();
    }

    public int getColor() {
        return color;
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.listener = listener;
    }

    public interface OnColorChangedListener {
        void onColorChanged(int newColor);
    }
}
