package com.hctord.green.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hctord.green.R;

public final class Utils {


    public static interface Predicate<T> {
        public boolean test(T args);
    }

    // Prevent creation
    private Utils(){}

    public static Point slope(Point a, Point b) {
        return new Point(b.x - a.x, b.y - a.y);
    }

    public static void slope(PointF a, PointF b, PointF result) {
        result.set(b.x - a.x, b.y - a.y);
    }
    public static void slope(Point a, Point b, Point result) {
        result.set(b.x - a.x, b.y - a.y);
    }

    public static Point delta(Point a, Point b) {
        return new Point(Math.abs(b.x - a.x), Math.abs(b.y - a.y));
    }

    public static void delta(Point a, Point b, Point result) {
        result.set(Math.abs(b.x - a.x), Math.abs(b.y - a.y));
    }

    public static void delta(PointF a, PointF b, PointF result) {
        result.set(Math.abs(b.x - a.x), Math.abs(b.y - a.y));
    }

    public static boolean within(Point point, Point bounds) {
        return (
                    point.x >= 0 &&
                    point.y >= 0 &&
                    point.x < bounds.x &&
                    point.y < bounds.y
                );
    }

    public static float clamp(float x, float min, float max) {
        return Math.min(min, Math.max(x, max));
    }

    public static float dist(Point a, Point b) {
        return (float)Math.sqrt(Math.pow(b.x-a.x, 2) + Math.pow(b.y-a.y, 2));
    }

    public static float dist(PointF a, PointF b) {
        return (float)Math.sqrt(Math.pow(b.x-a.x, 2) + Math.pow(b.y-a.y, 2));
    }

    public static PointF mid(PointF a, PointF b) {
        return new PointF((a.x + b.x)/2, (a.y + b.y)/2);
    }

    public static void mid(PointF a, PointF b, PointF result) {
        result.x = (a.x + b.x) / 2;
        result.y = (a.y + b.y) / 2;
    }

    public static int[] toSimpleTypeArray(List<Integer> list) {
        int[] output = new int[list.size()];
        for (int i = 0; i < output.length; ++i) {
            output[i] = list.get(i);
        }
        return output;
    }

    public static <T> Collection<T> where(Collection<T> source, Predicate<T> predicate) {
        Collection<T> output = new ArrayList<T>();
        for (T item : source) {
            if (predicate.test(item))
                output.add(item);
        }
        return output;
    }

    public static <T> T find(Collection<T> source, Predicate<T> predicate) {
        for (T item : source) {
            if (predicate.test(item))
                return item;
        }
        return null;
    }

    public static <T> void removeWhere(List<T> source, Predicate<T> predicate) {
        for (int i = 0; i < source.size(); i++) {
            if (predicate.test(source.get(i)))
                source.remove(i);
        }
    }

    public static void plot(Point from, Point to, RecycledArrayList<Point> result) {
        int dx = Math.abs(to.x - from.x);
        int dy = Math.abs(to.y - from.y);
        int sx, sy;
        int x = from.x, y = from.y;

        sx = from.x < to.x ? 1 : -1;
        sy = from.y < to.y ? 1 : -1;
        int err = dx - dy;
        int e2;

        while (true) {
            result.addAndRecycle(x, y);
            if (x == to.x && y == to.y)
                break;
            e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }

    public static byte[] shortArrayToByteArray(short[] input) {
        byte[] output = new byte[input.length * 2];
        for (int i = 0; i < input.length; i++) {
            output[i * 2] = (byte)((input[i] >> 8) & 0xFF);
            output[i * 2 + 1] = (byte)(input[i] & 0xFF);
        }
        return output;
    }

    public static short[] byteArrayToShortArray(byte[] input) {
        short[] output = new short[input.length / 2];
        for (int i = 0; i < output.length; i++) {
            output[i] = (short)((input[i * 2] << 8) | input[i * 2 + 1]);
        }
        return output;
    }

    public static byte[] intArrayToByteArray(int[] input) {
        byte[] output = new byte[input.length * 4];
        for (int i = 0; i < input.length; i++) {
            output[i * 4]     = (byte)((input[i] >> 24) & 0xFF);
            output[i * 4 + 1] = (byte)((input[i] >> 16) & 0xFF);
            output[i * 4 + 2] = (byte)((input[i] >> 8) & 0xFF);
            output[i * 4 + 3] = (byte)(input[i] & 0xFF);
        }
        return output;
    }

    public static byte[] intArrayToByteArray(Integer[] input) {
        byte[] output = new byte[input.length * 4];
        for (int i = 0; i < input.length; i++) {
            output[i * 4]     = (byte)((input[i] >> 24) & 0xFF);
            output[i * 4 + 1] = (byte)((input[i] >> 16) & 0xFF);
            output[i * 4 + 2] = (byte)((input[i] >> 8) & 0xFF);
            output[i * 4 + 3] = (byte)(input[i] & 0xFF);
        }
        return output;
    }

    public static int[] byteArrayToIntArray(byte[] input) {
        int[] output = new int[input.length / 4];
        for (int i = 0; i < output.length; i++) {
            output[i] = (((input[i * 4] & 0xFF) << 24)     |
                         ((input[i * 4 + 1] & 0xFF) << 16) |
                         ((input[i * 4 + 2] & 0xFF) << 8)  |
                         ((input[i * 4 + 3] & 0xFF)));
        }
        return output;
    }

    public static byte[] intToByteArray(int input) {
        return new byte[] {
                (byte)((input >> 24) & 0xFF),
                (byte)((input >> 16) & 0xFF),
                (byte)((input >> 8) & 0xFF),
                (byte)(input & 0xFF)
        };
    }

    public static int byteArrayToInt(byte[] input) {
        return ((input[0] << 24) |
                (input[1] << 16) |
                (input[2] << 8)  |
                input[3]);
    }

    public static <T> boolean arraysEqual(T[] a, T[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    public static void alert(Context ctx, int title, int text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

        builder.setTitle(title)
               .setMessage(text)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       dialog.dismiss();
                   }
               })
               .show();
    }

    public static <T> void clearArray(T[] arr, T value) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = value;
        }
    }

    public static <T> boolean arrayContains(T[] array, T value) {
        for (T t : array)
            if (t == value)
                return true;
        return false;
    }

    public static float getLuminance(int rgb) {
        int r = (rgb & 0x00FF0000) >> 16,
            g = (rgb & 0x0000FF00) >> 8,
            b = (rgb & 0x000000FF);
        float rf = r / 255.f,
              gf = g / 255.f,
              bf = b / 255.f;
        return 0.2126f * rf + 0.7152f * gf + 0.0722f * bf;
    }

    private static float[] hsv = new float[3];
    public static int maxSaturation(int rgb) {
        Color.colorToHSV(rgb, hsv);
        hsv[1] = 1;
        return Color.HSVToColor(hsv);
    }
}
