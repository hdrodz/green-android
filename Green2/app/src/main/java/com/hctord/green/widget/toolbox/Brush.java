package com.hctord.green.widget.toolbox;

import android.graphics.Point;

import com.hctord.green.document.PixelArt;
import com.hctord.green.widget.PixelEditorView2;
import com.hctord.green.util.Comparer;
import com.hctord.green.util.RecycledArrayList;
import com.hctord.green.util.Recycler;
import com.hctord.green.util.Utils;

public abstract class Brush {

    public static final Recycler<Point> POINT_RECYCLER = new Recycler<Point>() {
        @Override
        public void recycle(Point target, Object... args) {
            target.x = (Integer)args[0];
            target.y = (Integer)args[1];
        }
    };

    public static final Comparer<Point> POINT_COMPARER = new Comparer<Point>() {
        @Override
        public boolean compare(Point a, Point b) {
            return a.x == b.x && a.y == b.y;
        }
    };

    protected volatile RecycledArrayList<Point> modified;

    protected byte[] editingFrame;

    protected Point size;

    protected byte color;

    private int frame;

    private PixelEditorView2 editor;

    private PixelArt art;

    private boolean isCanceled = false;

    public Brush(PixelEditorView2 editor, int frame, byte color) {
        this.editor = editor;
        art = editor.getTarget();
        this.frame = frame;
        modified = new RecycledArrayList<>(POINT_RECYCLER, POINT_COMPARER, Point.class);
        this.color = color;
        refresh();
    }

    protected void setEditor(PixelEditorView2 editor) {
        this.editor = editor;
    }

    public byte getColor() {
        return color;
    }

    public void refresh() {
        art = editor.getTarget();
        editingFrame = art.getFrame(frame);
        size = art.getSize();
    }

    public int getFrame() {
        return frame;
    }

    public void setFrame(int frame) {
        this.frame = frame;
        refresh();
    }

    public void setColor(byte index) {
        color = index;
    }

    public void touchStart(Point position) {
        isCanceled = false;
    }

    public void touchDelta(Point position) {
        updatePreview();
    }

    public void touchEnd(Point position) {
        if (!isCanceled) {
            touchDelta(position);
            commitChanges();
        }
        else {
            isCanceled = false;
        }
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void cancel() {
        modified.clear();
        isCanceled = true;
    }

    private void commitChanges() {
        // TODO: tell the associated editor control to update the requested pixels
        editor.preCommitChanges();
        for (Point pt : modified) {
            if (Utils.within(pt, size))
                editingFrame[pt.y * size.x + pt.x] = color;
        }
        editor.commitChanges();
        modified.clear();
    }

    private void updatePreview() {
        // TODO: tell the associated editor control to update the preview
        editor.updatePreview(modified);
    }
}
