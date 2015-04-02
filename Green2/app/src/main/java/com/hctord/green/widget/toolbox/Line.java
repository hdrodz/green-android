package com.hctord.green.widget.toolbox;

import android.graphics.Point;

import com.hctord.green.widget.PixelEditorView2;
import com.hctord.green.util.Utils;

/**
 * Brush that draws lines.
 */
public final class Line extends Brush {

    Point start = new Point(), end = new Point();
    Point delta = new Point();

    public Line(PixelEditorView2 editor, int layer, byte color) {
        super(editor, layer, color);
    }

    @Override
    public void touchStart(Point position) {
        super.touchStart(position);
        start.set(position.x, position.y);
    }

    @Override
    public void touchDelta(Point position) {
        end.set(position.x, position.y);
        Utils.slope(start, end, delta);

        modified.clear();
        Utils.plot(start, end, modified);
        super.touchDelta(position);
    }
}
