package com.hctord.green.document;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;

import java.util.List;

import com.hctord.green.util.Utils;

/**
 * Utility class for rendering pixel art images to a bitmap.
 */
public class ImageRenderer {

    private final Utils.Predicate<Point> TOUCH_PREDICATE = new Utils.Predicate<Point>() {
        @Override
        public boolean test(Point pt) {
            return !Utils.within(pt, src.getSize());
        }
    };

    private Bitmap cache;
    private Bitmap editCache;
    private PixelArt src;
    private boolean editCacheInvalidated = true;

    public ImageRenderer() {
        this(null);
    }

    public ImageRenderer(PixelArt src) {
        switchSource(src);
    }

    public void switchSource(PixelArt newSrc) {
        src = newSrc;
        if (src != null) {
            if (cache != null)
                cache.recycle();
            cache = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
            updateCache(0);
            editCache = cache.copy(Bitmap.Config.ARGB_8888, true);
        }
    }

    /**
     * Creates a valid cached image containing the data of the current frame.
     * @param frame
     */
    public void createEditCache(int frame) {
        Integer[] palette = new Integer[src.getPalette().size()];
        src.getPalette().toArray(palette);
        int[] pixels = new int[src.getHeight() * src.getWidth()];
        int w = src.getWidth();
        byte[] srcLayer = src.getFrame(frame);
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < w; x++) {
                pixels[y * w + x] = palette[srcLayer[y * w + x]];
            }
        }
        editCache.setPixels(pixels, 0, w, 0, 0, w, src.getHeight());
        editCacheInvalidated = false;
    }

    public void updateEditCache(List<Point> edited, byte color, int frame) {
        if (editCacheInvalidated)
            createEditCache(frame);

        Integer[] palette = new Integer[src.getPalette().size()];
        src.getPalette().toArray(palette);
        int[] pixels = new int[src.getHeight() * src.getWidth()];
        int w = src.getWidth();
        editCache.eraseColor(0);
        editCache.getPixels(pixels, 0, w, 0, 0, w, src.getHeight());

        for (Point pt : edited) {
            if (!Utils.within(pt, src.getSize())) continue;
            if (color != 0)
                pixels[pt.y * w + pt.x] = palette[color];
            else
                pixels[pt.y * w + pt.x] = ~palette[src.getFrame(frame)[pt.y * w + pt.x]] | 0xFF000000;
        }
        editCache.setPixels(pixels, 0, w, 0, 0, w, src.getHeight());
    }

    public void discardEditCache() {
        editCacheInvalidated = true;
    }

    public Bitmap getEditCache() {
        return editCache;
    }

    @Deprecated
    public void updateCacheFrom(int startingLayer) {
        updateCache(startingLayer, src.getFrames().size() - 1);
    }

    public void updateCache(int frame) {
        // Get the image palette in an array for quick indexing
        Integer[] palette = new Integer[src.getPalette().size()];
        src.getPalette().toArray(palette);
        // Contains the color values that will be generated
        int[] pixels = new int[src.getHeight() * src.getWidth()];
        int w = src.getWidth();
        // Create the cached image if it is null.
        if (cache == null)
            cache = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);

        byte[] frameData = src.getFrame(frame);
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                // Skip this pixel if the color is transparent
                // TODO: implement alpha blending
                if (palette[frameData[y * w + x]].equals(Color.TRANSPARENT))
                    continue;

                pixels[y * w + x] = palette[frameData[y * w + x]];
            }
        }

        cache.setPixels(pixels, 0, w, 0, 0, w, src.getHeight());
    }

    /**
     * Renders a fragment of the image.
     * @param startingLayer First layer to render, inclusively.
     * @param endingLayer Last layer to render, inclusively.
     * @deprecated No longer does anything, use updateCache(frame).
     */
    @Deprecated
    public void updateCache(int startingLayer, int endingLayer) {
        // Get the image palette in an array for quick indexing
        Integer[] palette = new Integer[src.getPalette().size()];
        src.getPalette().toArray(palette);
        // Contains the color values that will be generated
        int[] pixels = new int[src.getHeight() * src.getWidth()];
        int w = src.getWidth();
        // Create the cached image if it is null.
        if (cache == null)
            cache = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);

        // Render the image in the range
        for (int i = startingLayer; i <= endingLayer; i++) {
            byte[] layer = src.getFrame(i);
            for (int y = 0; y < src.getHeight(); y++) {
                for (int x = 0; x < src.getWidth(); x++) {
                    // Skip this pixel if the color is transparent
                    // TODO: implement alpha blending
                    if (palette[layer[y * w + x]].equals(Color.TRANSPARENT))
                        continue;

                    pixels[y * w + x] = palette[layer[y * w + x]];
                }
            }
        }
        cache.setPixels(pixels, 0, w, 0, 0, w, src.getHeight());
    }

    public void updateCache(int startingLayer, List<Point> which) {
        // Get the image palette in an array for quick indexing
        Integer[] palette = new Integer[src.getPalette().size()];
        src.getPalette().toArray(palette);
        // Contains the color values in the target bitmap
        int[] pixels = new int[src.getHeight() * src.getWidth()];
        int w = src.getWidth();
        // Since we are only updating certain pixels, copy the existing data from the cache
        cache.getPixels(pixels, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());

        // Draw through the image layers
        for (int i = startingLayer; i < src.getFrames().size(); i++) {
            byte[] layer = src.getFrame(i);
            // Update the specified pixels
            for (Point pt : which) {
                if (!Utils.within(pt, src.getSize())) continue;
                // Skip this pixel if the color is transparent
                // TODO: implement alpha blending
                if (palette[layer[pt.y * w + pt.x]].equals(Color.TRANSPARENT))
                    continue;

                pixels[pt.y * w + pt.x] = palette[layer[pt.y * w + pt.x]];
            }
        }
        // Set the values in the target bitmap
        cache.setPixels(pixels, 0, w, 0, 0, w, src.getHeight());
    }

    public Bitmap getCache() {
        return cache;
    }

    /*
     * Force a redraw of the entire image to the cache.
     */
    @Deprecated
    public void updateCache() {
        updateCacheFrom(0);
    }

    public Bitmap copyCache() {
        return cache.copy(Bitmap.Config.ARGB_8888, false);
    }

    public Bitmap render() {
        updateCache();
        return cache.copy(Bitmap.Config.ARGB_8888, false);
    }
}
