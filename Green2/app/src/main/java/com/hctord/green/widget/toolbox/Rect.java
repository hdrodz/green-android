package com.hctord.green.widget.toolbox;

import android.graphics.Point;

import com.hctord.green.widget.PixelEditorView2;

public final class Rect extends Brush {

    private Point start = new Point(), end = new Point();

    public Rect(PixelEditorView2 editorView, int layer, byte color) {
        super(editorView, layer, color);
    }

    @Override
    public void touchStart(Point position) {
        super.touchStart(position);
        start.set(position.x, position.y);
    }

    @Override
    public void touchDelta(Point position) {
        end.set(position.x, position.y);
        int startX, startY, endX, endY;
        if (start.x < end.x) {
            startX = start.x;
            endX = end.x;
        }
        else {
            startX = end.x;
            endX = start.x;
        }

        if (start.y < end.y) {
            startY = start.y;
            endY = end.y;
        }
        else {
            startY = end.y;
            endY = start.y;
        }

        modified.clear();
        for (int x = startX; x <= endX; x++) {
            if (x == startX || x == endX) {
                for (int y = startY; y <= endY; y++) {
                    modified.addAndRecycle(x, y);
                }
            }
            else {
                modified.addAndRecycle(x, startY);
                modified.addAndRecycle(x, endY);
            }
        }
        super.touchDelta(position);
    }
}
