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

public class PixelArt implements Parcelable {

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
    private List<byte[]> layers;
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
        layers = new ArrayList<byte[]>();
        addLayer();
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
        layers = new ArrayList<byte[]>(layerCount);
        for (int i = 0; i < layerCount; ++i) {
            byte[] layer = new byte[size.x * size.y];
            in.readByteArray(layer);
            layers.add(layer);
        }
    }

    public Point getSize() {
        return size;
    }

    public int getWidth() { return size.x; }

    public int getHeight() { return size.y; }

    public void addLayer() {
        layers.add(new byte[size.y * size.x]);
    }

    public List<Integer> getPalette() {
        return palette;
    }

    public List<byte[]> getLayers() {
        return layers;
    }

    public int getMostUsedColor() {
        SparseIntArray colors = new SparseIntArray();
        for (byte[] layer : layers) {
            for (byte pixel : layer) {
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

    public byte[] getLayer(int position) {
        return layers.get(position);
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
        stream.write(layers.size());
        for (byte[] layer : layers) {
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
        this.layers = new ArrayList<byte[]>(b);
        for (int i = 0; i < b; i++) {
            buffer = new byte[size.x * size.y];
            stream.read(buffer);
            this.layers.add(buffer);
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
        out.writeInt(layers.size());
        for (byte[] layer : layers) {
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
