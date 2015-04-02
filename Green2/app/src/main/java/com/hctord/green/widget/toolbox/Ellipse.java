package com.hctord.green.widget.toolbox;

import android.graphics.Point;

import com.hctord.green.widget.PixelEditorView2;
import com.hctord.green.util.Utils;

public final class Ellipse extends Brush {

    private Point start, end;

    public Ellipse(PixelEditorView2 editor, int layer, byte color) {
        super(editor, layer, color);
    }

    @Override
    public void touchStart(Point position) {
        super.touchStart(position);
        start = position;
    }

    @Override
    public void touchDelta(Point position) {
        Point center, delta;
        int a, b;

        end = position;
        center = new Point((start.x + end.x) / 2, (start.y + end.y) / 2);
        delta = Utils.delta(start, end);

        a = delta.x / 2;
        b = delta.y / 2;

        if (delta.x >= delta.y) {

        }
        else {

        }

        modified.clear();
    }

}
