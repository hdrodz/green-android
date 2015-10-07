package com.hctord.green.document;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseIntArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.hctord.green.util.Utils;

public class PixelArt
        implements Parcelable {

    public static final int[] DEFAULT_PALETTE = {
            Color.TRANSPARENT,
            Color.BLACK,
            Color.WHITE,
    };

    private static final byte[] FILE_CODE = {
        0x74, // 't'
        0x75, // 'u'
        0x72, // 'r'
        0x74, // 't'
        0x6c, // 'l'
        0x65, // 'e'
        0x73, // 's'
        0x00  // null terminator to align to 8 bytes
    };

    private Point size;
    private List<byte[]> frames;
    private List<Integer> palette;

    public PixelArt(int width, int height) {
        this(new Point(width, height));
    }

    public PixelArt(Point size) {
        this.size = size;
        palette = new ArrayList<Integer>();
        for (int color : DEFAULT_PALETTE) {
            palette.add(color);
        }
        frames = new ArrayList<byte[]>();
        addFrame();
    }

    public PixelArt(InputStream stream) throws IOException {
        read(stream);
    }

    private PixelArt(Parcel in) {
        size = new Point(in.readInt(), in.readInt());
        int[] palette = new int[in.readInt()];
        in.readIntArray(palette);
        this.palette = new ArrayList<Integer>(palette.length);
        for (int color : palette) {
            this.palette.add(color);
        }
        int layerCount = in.readInt();
        frames = new ArrayList<byte[]>(layerCount);
        for (int i = 0; i < layerCount; ++i) {
            byte[] layer = new byte[size.x * size.y];
            in.readByteArray(layer);
            frames.add(layer);
        }
    }

    public Point getSize() {
        return size;
    }

    public int getWidth() { return size.x; }

    public int getHeight() { return size.y; }

    public void addFrame() {
        frames.add(new byte[size.y * size.x]);
    }

    public List<Integer> getPalette() {
        return palette;
    }

    public List<byte[]> getFrames() {
        return frames;
    }

    public int getMostUsedColor() {
        SparseIntArray colors = new SparseIntArray();
        for (byte[] frame : frames) {
            for (byte pixel : frame) {
                if (pixel == 0)
                    continue;
                if (colors.get(pixel, -1) == -1) {
                    colors.put(pixel, 1);
                }
                else {
                    colors.put(pixel, colors.get(pixel) + 1);
                }
            }
        }
        int mostFrequentIndex = -1, mostFrequentCount = -1;
        for (int i = 1; i < palette.size(); ++i) {
            if (colors.get(i, -1) > mostFrequentCount) {
                mostFrequentIndex = i;
                mostFrequentCount = colors.get(i);
            }
        }
        return palette.get(mostFrequentIndex) | 0xFF000000;
    }

    public int getAverageColor() {
        long total_r = 0,
             total_g = 0,
             total_b = 0;
        int nontransparent_pixels = 0;

        for (byte[] frame : frames) {
            for (byte pixel : frame) {
                if (pixel == 0)
                    continue;
                int rgb = palette.get(pixel);
                int r, g, b;
                r = (rgb & 0x00FF0000) >> 16;
                g = (rgb & 0x0000FF00) >> 8;
                b = (rgb & 0x000000FF);

                total_r += r;
                total_g += g;
                total_b += b;
                ++nontransparent_pixels;
            }
        }

        if (nontransparent_pixels == 0)
            return 0xFF000000;

        int r = (int)(total_r / nontransparent_pixels);
        int g = (int)(total_g / nontransparent_pixels);
        int b = (int)(total_b / nontransparent_pixels);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public byte[] getFrame(int position) {
        return frames.get(position);
    }

    public int frameCount() {
        return frames.size();
    }

    public void write(OutputStream stream) throws IOException {
        // Write the file code
        stream.write(FILE_CODE);
        // Write the image dimensions
        stream.write(Utils.shortArrayToByteArray(new short[]{(short) size.x, (short) size.y}));
        // Write the image palette
        stream.write(palette.size());
        Integer[] out = new Integer[palette.size()];
        palette.toArray(out);
        stream.write(Utils.intArrayToByteArray(out));
        // Write the image layer data
        stream.write(frames.size());
        for (byte[] layer : frames) {
            stream.write(layer);
        }
    }

    public void read(InputStream stream) throws IOException {
        byte[] buffer;
        short[] sbuffer;
        int[] ibuffer;
        byte b;

        // Read the 8 byte header
        buffer = new byte[8];
        stream.read(buffer);
        for (int i = 0; i < 8; i++) {
            if (buffer[i] != FILE_CODE[i]) throw new IllegalArgumentException("Stream is not in correct format.");
        }

        // Read the image size
        buffer = new byte[4];
        stream.read(buffer);
        sbuffer = Utils.byteArrayToShortArray(buffer);
        this.size = new Point(sbuffer[0], sbuffer[1]);

        // Read the palette data
        b = (byte)stream.read();
        this.palette = new ArrayList<Integer>(b);
        buffer = new byte[b * 4];
        stream.read(buffer);
        ibuffer = Utils.byteArrayToIntArray(buffer);
        for (int i : ibuffer) {
            this.palette.add(i);
        }

        // Read the layer data
        b = (byte)stream.read();
        this.frames = new ArrayList<byte[]>(b);
        for (int i = 0; i < b; i++) {
            buffer = new byte[size.x * size.y];
            stream.read(buffer);
            this.frames.add(buffer);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    //
    // Parcelable impl
    //
    ////////////////////////////////////////////////////////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(size.x);
        out.writeInt(size.y);
        int[] paletteAsArray = Utils.toSimpleTypeArray(palette);
        out.writeInt(paletteAsArray.length);
        out.writeIntArray(paletteAsArray);
        out.writeInt(frames.size());
        for (byte[] layer : frames) {
            out.writeByteArray(layer);
        }
    }

    public static final Creator<PixelArt> CREATOR = new Creator<PixelArt>() {
        @Override
        public PixelArt createFromParcel(Parcel parcel) {
            return new PixelArt(parcel);
        }

        @Override
        public PixelArt[] newArray(int len) {
            return new PixelArt[len];
        }
    };

}
