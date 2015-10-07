package com.hctord.green.widget.toolbox;

import android.graphics.Point;
import android.util.Log;

import com.hctord.green.widget.PixelEditorView2;
import com.hctord.green.util.RecycledArrayList;
import com.hctord.green.util.RecycledDeque;

/**
 * Brush that implements a flood fill algorithm.
 */
public final class Fill extends Brush {

    /*
     * Used to check whether the current flooder should cancel its work.
     */
    private static Flooder2 CURRENT_FLOODER;

    /*
     * Object for locking threads
     */
    private static final Object PIXEL_LOCK = new Object();

    private Flooder2 flooderThread;

    /**
     * Used for performing a flood fill on a background thread.
     */
    private class Flooder2 implements Runnable {
        private RecycledArrayList<Point> selectedPoints = new RecycledArrayList<Point>(
                POINT_RECYCLER, POINT_COMPARER, Point.class
        );
        private RecycledDeque<Point> toExplore = new RecycledDeque<Point>(
                POINT_RECYCLER, POINT_COMPARER, Point.class
        );
        private Point start;

        private boolean[] explored = new boolean[size.x * size.y];

        public boolean keepGoing = true;
        public void setStart(Point pt) {
            start = pt;
        }
        @Override
        public void run() {
            synchronized (PIXEL_LOCK) {
                CURRENT_FLOODER = this;
                Log.v("Flooder", "Ext thread lock: " + PIXEL_LOCK.hashCode());
                selectedPoints.clear();
                toExplore.clear();
                for (int i = 0; i < explored.length; i++) {
                    explored[i] = false;
                }

                final int w = size.x;
                final int h = size.y;

                if (start.x < 0 || start.x >= w || start.y < 0 || start.y >= h)
                    return;

                byte initialColor = editingFrame[start.y * w + start.x];

                toExplore.addAndRecycle(start.x, start.y);
                while (!toExplore.isEmpty() && keepGoing) {
                    Point p = toExplore.remove();

                    int x = p.x, y = p.y;
                    int x2, y2;
                    selectedPoints.addAndRecycle(p.x, p.y);

                    x2 = x;
                    if (y - 1 >= 0) {
                        y2 = y - 1;
                        if (!explored[y2 * w + x2]) {
                            if (editingFrame[y2 * w + x2] == initialColor)
                                toExplore.addAndRecycle(x2, y2);
                            explored[y2 * w + x2] = true;
                        }
                    }
                    if (y + 1 < h) {
                        y2 = y + 1;
                        if (!explored[y2 * w + x2]) {
                            if (editingFrame[y2 * w + x2] == initialColor)
                                toExplore.addAndRecycle(x2, y2);
                            explored[y2 * w + x2] = true;
                        }
                    }
                    y2 = y;
                    if (x - 1 >= 0) {
                        x2 = x - 1;
                        if (!explored[y2 * w + x2]) {
                            if (editingFrame[y2 * w + x2] == initialColor)
                                toExplore.addAndRecycle(x2, y2);
                            explored[y2 * w + x2] = true;
                        }
                    }
                    if (x + 1 < w) {
                        x2 = x + 1;
                        if (!explored[y2 * w + x2]) {
                            if (editingFrame[y2 * w + x2] == initialColor)
                                toExplore.addAndRecycle(x2, y2);
                            explored[y2 * w + x2] = true;
                        }
                    }
                }

                if (keepGoing) {
                    for (Point pt : selectedPoints)
                        modified.addAndRecycle(pt.x, pt.y);
                }
                CURRENT_FLOODER = null;
                PIXEL_LOCK.notifyAll();
            }
        }
    }

    public Fill(PixelEditorView2 editor, int layer, byte color) {
        super(editor, layer, color);
        flooderThread = new Flooder2();
    }

    @Override
    public void touchStart(Point position) {
        super.touchStart(position);
        flooderThread.setStart(position);
        Log.v("Flood", "Start flooding");
        new Thread(flooderThread, "flooder").start();
    }

    @Override
    public void touchDelta(Point position) {
        super.touchDelta(position);
    }

    @Override
    public void touchEnd(Point position) {
        synchronized (PIXEL_LOCK) {
            Log.v("Flooder", "Main thread lock: " + PIXEL_LOCK.hashCode());
            if (CURRENT_FLOODER != null) {
                try {
                    PIXEL_LOCK.wait(500);
                } catch (InterruptedException|IllegalMonitorStateException e) {
                    e.printStackTrace();
                }
            }
            Log.v("Flood", "Done flooding");
        }
        super.touchEnd(position);
    }
}
