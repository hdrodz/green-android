package com.hctord.green.widget.toolbox;

import android.graphics.Point;
import com.hctord.green.widget.PixelEditorView2;
import com.hctord.green.util.Utils;

/**
 * Brush that allows creating freeform lines.
 */
public class Freeform extends Brush {

    private Point last;

    public Freeform(PixelEditorView2 editor, int layer, byte color) {
        super(editor, layer, color);
    }

    @Override
    public void touchStart(Point position) {
        super.touchStart(position);
        last = position;
        modified.addAndRecycle(position.x, position.y);
    }

    @Override
    public void touchDelta(Point position) {
        Point delta = Utils.slope(last, position);
        Utils.plot(last, position, modified);
        last = position;
        //super.modified.addAll(modified);
        //super.modified.addAndRecycle(position.x, position.y);
        super.touchDelta(position);
    }
}
