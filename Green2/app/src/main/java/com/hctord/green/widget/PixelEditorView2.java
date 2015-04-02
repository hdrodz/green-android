package com.hctord.green.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.hctord.green.R;
import com.hctord.green.widget.toolbox.Brush;
import com.hctord.green.widget.toolbox.Ellipse;
import com.hctord.green.widget.toolbox.Eraser;
import com.hctord.green.widget.toolbox.Fill;
import com.hctord.green.widget.toolbox.Freeform;
import com.hctord.green.document.ImageRenderer;
import com.hctord.green.widget.toolbox.Line;
import com.hctord.green.document.PixelArt;
import com.hctord.green.widget.toolbox.Rect;

import java.util.List;

/**
 * Updated, cleaner version of the PixelEditorView control.
 */
public class PixelEditorView2
        extends View
        implements
            ScaleGestureDetector.OnScaleGestureListener,
            GestureDetector.OnGestureListener {

    // Editor variables

    private PixelArt target;
    private Brush brush;
    private boolean isDrawing;
    private byte storedColor;
    private boolean drawGrid = true;
    private OnEditListener onEditListener;

    // Layout variables

    private RectF actualSize;
    private RectF modifiedSize;
    private PointF actualOffset;
    private PointF modifiedOffset;
    private float zoom;

    // Render variables

    private Paint previewPaint;
    private Paint gridPaint;
    private Paint borderPaint;
    private Paint checkerPaint;
    private Bitmap rendered;
    private Bitmap edit;
    private ImageRenderer targetRenderer;

    // Manipulation variables

    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    // Private methods

    public void center() {
        actualOffset.set(0, 0);
        recalculateView();
    }

    private void recalculateView() {
        float centerX, centerY;
        centerX = getWidth() / 2f;
        centerY = getHeight() / 2f;
        float w, h;
        w = actualSize.width();
        h = actualSize.height();
        float diffX, diffY;
        diffX = w * zoom / 2;
        diffY = h * zoom / 2;

        modifiedOffset.set(actualOffset.x * zoom, actualOffset.y * zoom);
        modifiedSize.set(
                centerX + modifiedOffset.x + actualSize.left * zoom - diffX,
                centerY + modifiedOffset.y + actualSize.top * zoom - diffY,
                centerX + modifiedOffset.x + actualSize.right * zoom - diffX,
                centerY + modifiedOffset.y + actualSize.bottom * zoom - diffY
        );
        invalidate();
    }

    public PixelEditorView2(Context context, AttributeSet attrs) {
        super(context, attrs);

        Resources res = context.getResources();

        actualSize = new RectF();
        modifiedSize = new RectF();
        actualOffset = new PointF();
        modifiedOffset = new PointF();

        Bitmap checker = BitmapFactory.decodeResource(res, R.drawable.checkerboard);
        checkerPaint = new Paint();
        checkerPaint.setShader(new BitmapShader(checker, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));

        previewPaint = new Paint();
        previewPaint.setAntiAlias(false);

        gridPaint = new Paint();
        gridPaint.setColor(Color.BLACK);
        gridPaint.setStrokeWidth(0);

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeWidth(4);

        scaleGestureDetector = new ScaleGestureDetector(context, this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            scaleGestureDetector.setQuickScaleEnabled(false);
        gestureDetector = new GestureDetector(context, this);

        targetRenderer = new ImageRenderer();
        setTarget(new PixelArt(32, 32));
        brush = new Freeform(this, 0, (byte)1);
    }

    public void invalidateRenderCache() {
        targetRenderer.updateCache();
    }

    public PixelArt getTarget() {
        return target;
    }

    public boolean drawsGrid() {
        return drawGrid;
    }

    public void setDrawGrid(boolean drawGrid) {
        this.drawGrid = drawGrid;
    }

    public void setTarget(PixelArt target) {
        this.target = target;
        zoom = 16;
        targetRenderer.switchSource(target);
        rendered = targetRenderer.getCache();
        edit = targetRenderer.getEditCache();
        actualSize.set(0, 0, target.getWidth(), target.getHeight());
        if (brush != null)
            brush.refresh();
        center();
    }

    public void setBrush(BrushType type) {

        switch (type) {
        case FREEFORM:
            brush = new Freeform(this, 0, brush instanceof Eraser ? storedColor : brush.getColor());
            break;
        case ERASER:
            storedColor = brush.getColor();
            brush = new Eraser(this, 0);
            break;
        case RECT:
            brush = new Rect(this, 0, brush instanceof Eraser ? storedColor : brush.getColor());
            break;
        case ELLIPSE:
            brush = new Ellipse(this, 0, brush instanceof Eraser ? storedColor : brush.getColor());
            break;
        case LINE:
            brush = new Line(this, 0, brush instanceof Eraser ? storedColor : brush.getColor());
            break;
        case FILL:
            brush = new Fill(this, 0, brush instanceof Eraser ? storedColor : brush.getColor());
            break;
        }
    }

    public void setOnEditListener(OnEditListener listener) {
        this.onEditListener = listener;
    }

    public void setBrushColor(int brushColor) {
        if (brush != null)
            brush.setColor((byte)brushColor);
    }

    public void preCommitChanges() {
        // TODO: save history stack here
        if (onEditListener != null)
            onEditListener.onPreEdit();
    }

    public void commitChanges() {
        targetRenderer.updateCache();
        targetRenderer.discardEditCache();
        invalidate();
        if (onEditListener != null)
            onEditListener.onEdit();
    }

    public void updatePreview(List<Point> modified) {
        targetRenderer.updateEditCache(modified, brush.getColor(), 0);
    }

    ////////////////////////////////////////////////////////////////////////
    //
    // View impl
    //
    ////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        if (event.getPointerCount() == 1) {
            int action = event.getActionMasked(),
                pointerIndex = event.getActionIndex();
            MotionEvent.PointerCoords coords;
            int width = getWidth(),
                height = getHeight();
            float rWidth = modifiedSize.width(),
                  rHeight = modifiedSize.height();
            int ex, ey;

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                {
                    isDrawing = true;
                    coords = new MotionEvent.PointerCoords();
                    event.getPointerCoords(pointerIndex, coords);

                    targetRenderer.createEditCache(0);

                    ex = (int)((coords.x - (width - rWidth) / 2 - modifiedOffset.x) / zoom);
                    ey = (int)((coords.y - (height - rHeight) / 2 - modifiedOffset.y) / zoom);

                    brush.touchStart(new Point(ex, ey));

                    invalidate();
                }
                break;
                case MotionEvent.ACTION_MOVE:
                {
                    if (isDrawing && !brush.isCanceled()) {
                        coords = new MotionEvent.PointerCoords();
                        event.getPointerCoords(pointerIndex, coords);

                        ex = (int) ((coords.x - (width - rWidth) / 2 - modifiedOffset.x) / zoom);
                        ey = (int) ((coords.y - (height - rHeight) / 2 - modifiedOffset.y) / zoom);

                        brush.touchDelta(new Point(ex, ey));

                        invalidate();
                    }
                    else {
                        brush.cancel();
                    }
                }
                break;
                case MotionEvent.ACTION_UP:
                {
                    if (isDrawing && !brush.isCanceled()) {
                        coords = new MotionEvent.PointerCoords();
                        event.getPointerCoords(pointerIndex, coords);

                        ex = (int) ((coords.x - (width - rWidth) / 2 - modifiedOffset.x) / zoom);
                        ey = (int) ((coords.y - (height - rHeight) / 2 - modifiedOffset.y) / zoom);

                        brush.touchEnd(new Point(ex, ey));

                        isDrawing = false;
                        invalidate();
                    }
                }
                break;
            }
        }
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        recalculateView();

        canvas.drawRect(0, 0, getWidth(), getHeight(), checkerPaint);

        canvas.drawRect(modifiedSize, borderPaint);
        canvas.drawBitmap(rendered, null, modifiedSize, previewPaint);

        if (isDrawing) {
            canvas.drawBitmap(edit, null, modifiedSize, previewPaint);
        }

        if (drawGrid && zoom >= 4) {
            int w = target.getWidth(), h = target.getHeight();
            float sx = modifiedSize.left, sy = modifiedSize.top,
                  ex = modifiedSize.right, ey = modifiedSize.bottom;
            for (int x = 1; x < w; ++x) {
                canvas.drawLine(sx + x * zoom, sy, sx + x * zoom, ey, gridPaint);
            }
            for (int y = 1; y < h; ++y) {
                canvas.drawLine(sx, sy + y * zoom, ex, sy + y * zoom, gridPaint);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////
    //
    // ScaleGestureDetector.OnScaleGestureListener impl
    //
    ////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        zoom *= scaleGestureDetector.getScaleFactor();
        recalculateView();
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

    }

    ////////////////////////////////////////////////////////////////////////
    //
    // GestureDetector.OnGestureListener impl
    //
    ////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
        if (e2.getPointerCount() >= 2) {
            modifiedOffset.offset(-dx, -dy);
            actualOffset.offset(-dx / zoom, -dy / zoom);
            if (isDrawing) {
                brush.cancel();
                isDrawing = false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float dx, float dy) {
        return false;
    }



    ////////////////////////////////////////////////////////////////////////
    //
    // Inner classes
    //
    ////////////////////////////////////////////////////////////////////////

    public static enum BrushType {
        FREEFORM,
        ERASER,
        FILL,
        LINE,
        RECT,
        ELLIPSE
    }

    public static interface OnEditListener {
        public void onPreEdit();
        public void onEdit();
    }
}
