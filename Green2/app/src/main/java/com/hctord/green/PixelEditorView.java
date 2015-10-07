package com.hctord.green;

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
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import com.hctord.green.widget.toolbox.Brush;
import com.hctord.green.document.ImageRenderer;
import com.hctord.green.document.PixelArt;
import com.hctord.green.document.history.HistoryManager;
import com.hctord.green.util.Utils;

/**
 * Custom control that provides a method of interacting with an image
 */
@Deprecated
public class PixelEditorView extends View {

    // Constants
    private static final String KEY_OFFSET_X = "offx",
                                KEY_OFFSET_Y = "offy",
                                KEY_ZOOM = "zoom";

    private static final int TOP_LEFT = 0,
                             TOP_RIGHT = 1,
                             BOTTOM_LEFT = 2,
                             BOTTOM_RIGHT = 3;

    private static final PointF CENTER_SCREEN = new PointF();


    private OnEditListener listener = null;

    // Drawing resources
    private Paint fingerLocatorPaint, rectPaint, zoomBgPaint, zoomTextPaint, imagePaint, gridPaint, debugPaint, backgroundPaint;
    private Bitmap rendered, edit;
    private BitmapDrawable checkerboard;
    private RectF targetRect;
    private RectF zoomBgRect;
    private DecimalFormat df;
    private boolean drawGrid = true;

    private PixelArt target;
    private boolean cleanSlate = true;
    private HistoryManager historyManager;
    private Brush brush;
    private ImageRenderer targetRenderer;
    private Point touchEvent;
    private PointF offset, touchOffset, centerTouchPoint = new PointF(), centerPointOffset = new PointF();
    private int clientWidth, clientHeight, zoomPointerId, primaryPointerId, editingLayer;

    // Touch resources
    private SparseArray<PointF> fingers;
    private PointF focalPoint = null;
    private int fingerCount = 0;
    private float zoom, zoomMultiplier, tempZoom = 0, zoom1x, factorX, factorY;
    private boolean isTouching, isZooming;

    public PixelEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Get the context's resources for quicker access
        Resources resources = context.getResources();
        // Create the checkered background
        Bitmap checker = BitmapFactory.decodeResource(resources, R.drawable.checkerboard);
        backgroundPaint = new Paint();
        backgroundPaint.setShader(new BitmapShader(checker, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));

        // Create a new image renderer that will render the target image
        this.targetRenderer = new ImageRenderer();

        // Create necessary points so that null checking isn't necessary
        offset = new PointF();
        touchEvent = new Point();
        touchOffset = new PointF();
        centerTouchPoint = new PointF();

        // Set the target image to a blank 32x32 image
        setTarget(new PixelArt(new Point(32, 32)));

        // Set the default zoom to 16.
        this.zoom = 16;

        // Create the finger array
        fingers = new SparseArray<PointF>(10);

        // Create necessary drawing resources
        imagePaint = new Paint();
        imagePaint.setAntiAlias(false);

        rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(4);
        rectPaint.setColor(Color.BLACK);

        debugPaint = new Paint();
        debugPaint.setColor(Color.RED);
        debugPaint.setTextSize(24);

        fingerLocatorPaint = new Paint();
        fingerLocatorPaint.setStrokeWidth(0);

        zoomBgPaint = new Paint();
        zoomBgPaint.setColor(0x80000000);
        zoomBgPaint.setAntiAlias(true);

        zoomTextPaint = new Paint();
        zoomTextPaint.setColor(Color.WHITE);
        zoomTextPaint.setStyle(Paint.Style.FILL);
        zoomTextPaint.setAntiAlias(true);
        zoomTextPaint.setTextAlign(Paint.Align.CENTER);
        zoomTextPaint.setTextSize(48);

        gridPaint = new Paint();
        gridPaint.setColor(Color.BLACK);
        gridPaint.setStrokeWidth(0);

        // Formatter so that the zoom text only has 2 trailing decimal points.
        df = new DecimalFormat();
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
        df.setRoundingMode(RoundingMode.DOWN);

        // Get the client width & height once layout is done.
        ViewTreeObserver observer = getViewTreeObserver();
        if (observer != null)
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    clientWidth = getWidth();
                    clientHeight = getHeight();
                    zoomBgRect = new RectF(clientWidth/2 - 160, 0,
                            clientWidth/2 + 160, 80);
                    CENTER_SCREEN.x = clientWidth / 2;
                    CENTER_SCREEN.y = clientHeight / 2;
                    center();
                }
            });
    }

    public void setListener(OnEditListener listener) {
        this.listener = listener;
    }

    private void moveTargetRect(float x, float y) {
        targetRect.left += x;
        targetRect.top += y;
        invalidate();
    }

    /**
     * Sets the image that the control will modify.
     * @param target Image that will be modified.
     */
    public void setTarget(PixelArt target) {
        setTarget(target, false);
    }

    public void setTarget(PixelArt target, boolean restoreHistory) {
        this.target = target;
        //setBrush(new Freeform(this, 0, (byte) 1));
        this.targetRenderer.switchSource(target);
        rendered = targetRenderer.getCache();
        edit = targetRenderer.getEditCache();
        center();
        invalidate();
        //this.historyManager = new HistoryManager(target);
        //if (restoreHistory) {
        //     if (Utils.arrayContains(getContext().fileList(), "history")) {
        //        historyManager.load(getContext());
        //    }
        //}
        cleanSlate = false;

    }

    /**
     * Sets the current tool that will be used to edit this image.
     * @param brush Tool that will edit the image.
     */
    public void setBrush(Brush brush) {
        this.brush = brush;
    }

    public void center(float x, float y) {
        offset.x = ((x - ((target.getWidth() * zoom) / 2)));
        offset.y = ((y - ((target.getHeight() * zoom) / 2)));
        if (targetRect == null)
            targetRect = new RectF();

        recalculateTargetRect();
        invalidate();
    }

    /**
     * Centers the target drawing area
     */
    public void center() {
        if (clientWidth == 0 || clientHeight == 0) {
            clientWidth = getWidth();
            clientHeight = getHeight();
        }
        center(clientWidth / 2, clientHeight / 2);
    }

    public void center(float x, float y, PointF deltaPoint) {
        center(x + deltaPoint.x, y + deltaPoint.y);
    }

    /**
     * Recalculates the target drawing area only taking into account offset & zoom.
     */
    private void recalculateTargetRect() {

        targetRect.set(offset.x, offset.y, offset.x + (int) (target.getWidth() * zoom),
                offset.y + (int) (target.getHeight() * zoom));
    }

    /**
     * Recalculates the target drawing area, taking into account sliding.
     * @param deltaX Change in x of the primary finger
     * @param deltaY Change in y of the primary finger
     * @param a Primary finger
     * @param b Secondary finger
     */
    private void recalculateTargetRect(float deltaX, float deltaY, PointF a, PointF b) {
        // Get the finger which is closest to the upper left corner
        int x, y;
        x = (int)(a.x <= b.x ? a.x : b.x);
        y = (int)(a.y <= b.y ? a.y : b.y);

        centerTouchPoint.x += deltaX;
        centerTouchPoint.y += deltaY;

        center(centerTouchPoint.x, centerTouchPoint.y, centerPointOffset);
        // Recalculate using the offset
    }

    private void zoomIntoTargetRect(float deltaX, float deltaY, float pinchX, float pinchY, float focalX, float focalY) {
        float rightFactor, bottomFactor;
        int x = (int)((focalX - offset.x) / zoom),
            y = (int)((focalY - offset.y) / zoom);

        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x >= target.getWidth()) x = target.getWidth();
        if (y >= target.getHeight()) y = target.getHeight();

        rightFactor = 1 - (float)x / target.getWidth();
        bottomFactor = 1 - (float)y / target.getHeight();

        offset.x = (int)(offset.x + deltaX / rightFactor);
        offset.y = (int)(offset.x + deltaY / bottomFactor);

        recalculateTargetRect();
    }

    /**
     * Set the layer that will be modified on touch.
     * @param layer Layer to be modified
     */
    public void setEditingLayer(int layer) {
        editingLayer = layer;
    }

    /**
     * Gets the layer that will be modified on touch.
     * @return Layer that is being modified
     */
    public int getEditingLayer() {
        return editingLayer;
    }

    /**
     * Gets the image that this control modifies.
     * @return Image that is being modified.
     */
    public PixelArt getTarget() {
        return target;
    }

    /**
     * Update the preview of the image.
     * @param modified Points modified by the function.
     */
    public void updatePreview(List<Point> modified) {
        targetRenderer.updateEditCache(modified, brush.getColor(), 0);
    }


    byte[] __changeBefore;

    public void preCommitChanges() {
        __changeBefore = target.getFrame(brush.getFrame()).clone();
    }

    public void commitChanges() {
        targetRenderer.updateCache();
        targetRenderer.discardEditCache();
        invalidate();
//        historyManager.saveToHistory(editingFrame);
        if (listener != null)
            listener.onEdit();
    }

    public boolean canUndo() {
        return //historyManager.canUndo(editingFrame);
        false;
    }

    public boolean canRedo() {
        return //historyManager.canRedo(editingFrame);
        false;
    }

    public void undo() {
        historyManager.undo(editingLayer);
        if (listener != null)
            listener.onEdit();
    }

    public void redo() {
        historyManager.redo(editingLayer);
        if (listener != null)
            listener.onEdit();
    }

    /**
     * Called when the system orders the view to be re-drawn.
     * @param canvas Canvas that will manage the drawing.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the checkerboard background as an indicator of alpha transparency.
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
        // TODO: Draw reference image
        // Draw the border rectangle
        canvas.drawRect(targetRect, rectPaint);
        // FIX: Draw image
        canvas.drawBitmap(rendered, null, targetRect, imagePaint);


        if (isTouching) {
            canvas.drawBitmap(edit, null, targetRect, imagePaint);
            PointF primary = fingers.get(primaryPointerId);
            // TODO: Draw image edit preview
            if (primary != null) {
                // Draw a debugging cursor showing the position of the primary finger
                canvas.drawLine(0, primary.y, clientWidth, primary.y, fingerLocatorPaint);
                canvas.drawLine(primary.x, 0, primary.x, clientHeight, fingerLocatorPaint);
            }
            if (isZooming) {
                // Draw the current zoom indicator
                int xPos = clientWidth / 2;
                int yPos = (int)((80 / 2) - ((zoomTextPaint.descent() + zoomTextPaint.ascent()) / 2)) ;
                canvas.drawRoundRect(zoomBgRect, 16, 16, zoomBgPaint);
                canvas.drawText(df.format(zoom), xPos, yPos, zoomTextPaint);
            }
        }

        if (drawGrid && zoom >= 4) {
            int w = target.getWidth(), h = target.getHeight();
            for (int x = 1; x < w; x++) {
                canvas.drawLine(x * zoom + offset.x, offset.y,
                        x * zoom + offset.x, h * zoom + offset.y, gridPaint);
            }
            for (int y = 1; y < h; y++) {
                canvas.drawLine(offset.x, y * zoom + offset.y,
                        w * zoom + offset.x, y * zoom + offset.y, gridPaint);
            }
        }

        //canvas.drawText(String.format("touchOffset: %s; centerPointOffset: %s", touchOffset.toString(), centerPointOffset.toString()), 0, clientHeight, this.debugPaint);
    }

    /**
     * Called when the system sends a touch event to the control.
     * @param event Event data
     * @return Whether the request was handled by the control.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction(),
            pointerIndex = event.getActionIndex(),
            pointerId = event.getPointerId(pointerIndex);
        MotionEvent.PointerCoords coords;

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:               // Primary finger down
            {
                PointF pt;
                isTouching = true;

                primaryPointerId = pointerId;
                coords = new MotionEvent.PointerCoords();
                targetRenderer.createEditCache(editingLayer);
                event.getPointerCoords(pointerIndex, coords);
                pt = new PointF(coords.x, coords.y);

                fingers.put(pointerId, pt);
                fingerCount++;

                touchEvent.x = (int)((pt.x - offset.x)/zoom);
                touchEvent.y = (int)((pt.y - offset.y)/zoom);

                brush.touchStart(touchEvent);
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN:       // Secondary finger down
            {
                PointF pt;
                fingerCount++;

                coords = new MotionEvent.PointerCoords();
                event.getPointerCoords(pointerIndex, coords);

                pt = new PointF(coords.x, coords.y);

                // If this is the second pointer, then this is the pointer that will be used
                // to calculate the delta in zoom.
                if (fingerCount == 2) {
                    isZooming = true;
                    zoomPointerId = pointerId;
                    tempZoom = 0;
                    int w2 = target.getWidth() / 2, h2 = target.getHeight() / 2;
                    // Set the touch offset to be the one closest to the upper-left corner of the
                    // rect.
                    PointF ptA = fingers.get(primaryPointerId);
                    if (ptA != null) {
                        int x = (int)(ptA.x <= pt.x ? ptA.x : pt.x);
                        int y = (int)(ptA.y <= pt.y ? ptA.y : pt.y);
                        touchOffset.x = offset.x;
                        touchOffset.y = offset.y;
                    }
                }

                fingers.put(pointerId, pt);
                break;
            }
            case MotionEvent.ACTION_MOVE:               // Finger moved
            {
                int pCount = event.getPointerCount();
                float x1 = 0, x1b = 0, x2, x2b, y1 = 0, y1b = 0, y2, y2b;
                PointF prevFocalPoint = new PointF();

                // Try to get the pre-move position of the primary finger to get a delta.
                {
                    PointF primary = fingers.get(primaryPointerId);
                    PointF secondary = fingers.get(zoomPointerId);
                    if (primary != null) {
                        x1 = primary.x;
                        y1 = primary.y;
                    }
                    else {
                        primary = new PointF(0, 0);
                    }
                    if (secondary != null) {
                        x1b = secondary.x;
                        y1b = secondary.y;
                    }
                    else {
                        secondary = new PointF(0, 0);
                    }
                    Utils.mid(primary, secondary, prevFocalPoint);
                }

                // Update the points in the list to contain up-to-date pointer movements.
                for (int i = 0; i < pCount; i++) {
                    PointF pt = fingers.get(event.getPointerId(i));
                    if (pt != null) {
                        pt.x = event.getX(i);
                        pt.y = event.getY(i);
                    }
                }

                // If there is only one finger on the screen, do the default action for touch, else
                // zoom/pan.
                if (fingerCount == 1 && !brush.isCanceled()) {
                    PointF pt = fingers.get(primaryPointerId);
                    if (pt != null) {
                        touchEvent.x = (int)((pt.x - offset.x)/zoom);
                        touchEvent.y = (int)((pt.y - offset.y)/zoom);
                        brush.touchDelta(touchEvent);
                    }
                }
                else {
                    PointF ptA = fingers.get(primaryPointerId),
                           ptB = fingers.get(zoomPointerId);


                    // If tempZoom is 0 then reset all of the zoom calculation parameters,
                    // else update the current zoom.
                    if (tempZoom == 0 && ptA != null && ptB != null) {
                        if (focalPoint == null) {
                            focalPoint = Utils.mid(ptA, ptB);
                        }
                        else {
                            Utils.mid(ptA, ptB, focalPoint);
                        }
                        brush.cancel();
                        targetRenderer.discardEditCache();
                        isZooming = true;
                        tempZoom = zoom;
                        zoom1x = zoomMultiplier = Utils.dist(ptA, ptB);
                        centerTouchPoint.set(offset.x + ((target.getWidth() * zoom) / 2),
                                offset.y + (target.getHeight() * zoom) / 2);
                        centerPointOffset.set(0, 0);
                        // force a recalculation on next pass to avoid division by 0
                        if (zoom1x == 0)
                            tempZoom = 0;
                    }
                    else if (ptA != null && ptB != null) {
                        if (focalPoint == null) {
                            focalPoint = Utils.mid(ptA, ptB);
                        }
                        else {
                            Utils.mid(ptA, ptB, focalPoint);
                        }
                        Utils.delta(prevFocalPoint, focalPoint, centerPointOffset);
                        x2 = ptA.x;
                        y2 = ptA.y;
                        x2b = ptB.x;
                        y2b = ptB.y;
                        zoomMultiplier = Utils.dist(ptA, ptB);
                        zoom = tempZoom * (zoomMultiplier / zoom1x);
                        // Take panning into account when recalculating the target area.
                        if (x1 != 0 && y1 != 0 && x1b != 0 && y1b != 0) {
                            recalculateTargetRect(((x2 - x1) + (x2b - x1b))/ 2,
                                    ((y2 - y1) + (y2b - y1b)) / 2, ptA, ptB);
                            //zoomIntoTargetRect(((x2 - x1) + (x2b - x1b)) / 2,
                            //        ((y2 - y1) + (y2b - y1b)) / 2, pinchX, pinchY, focalPoint.x, focalPoint.y);
                        }
                    }
                }

                break;
            }
            case MotionEvent.ACTION_POINTER_UP:         // Secondary finger up
            {
                // Remove the lifted pointer
                fingers.remove(pointerId);
                // Decrease the finger count
                fingerCount--;
                // Cancel zooming if there is only 1 finger left.
                if (fingerCount <= 1) {
                    isZooming = false;
                    tempZoom = 0;
                    focalPoint = null;
                }
                break;
            }
            case MotionEvent.ACTION_UP:                 // Primary finger up
            {
                fingerCount = 0;
                isTouching = false;

                coords = new MotionEvent.PointerCoords();
                event.getPointerCoords(pointerIndex, coords);

                touchEvent.x = (int)((coords.x - offset.x)/zoom);
                touchEvent.y = (int)((coords.y - offset.y)/zoom);

                // Commit changes to the image
                brush.touchEnd(touchEvent);

                // Clear the finger list since the primary finger is out.
                fingers.clear();
                break;
            }
            default:
                return false;
        }

        // Force a redraw if handled.
        invalidate();
        return true;
    }

    public Brush getBrush() {
        return brush;
    }

    public void saveHistory() {
        // TODO
    }

    public static interface OnEditListener {
        public void onEdit();
    }
}
