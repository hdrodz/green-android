package com.hctord.green.document.history;

import android.graphics.Point;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.hctord.green.document.PixelArt;
import com.hctord.green.util.Utils;

/**
 * History entry that represents the base state of the image on
 * first load.
 */
public class BaseImageHistoryEntry implements HistoryEntry {
    private static final int ID = 0;

    static {
        HistoryEntry.CLASS_MAP.append(ID, BaseImageHistoryEntry.class);
    }

    private List<byte[]> baseImage = new ArrayList<byte[]>();
    private Point size;

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public void init(Object...args) {
        if (!(args[0] instanceof PixelArt))
            throw new IllegalArgumentException("First arg must be pixel art");
        boolean firstInit = true;
        if (args.length > 1) {
            firstInit = (Boolean) args[1];
        }
        PixelArt pa = (PixelArt) args[0];
        size = pa.getSize();
        if (firstInit) {
            baseImage = pa.getFrames().subList(0, pa.getFrames().size());
        }
    }

    @Override
    public void undo(PixelArt art) {
        art.getFrames().clear();
        for (byte[] layer : baseImage)
            art.getFrames().add(layer);
    }

    @Override
    public void redo(PixelArt art) {
        throw new UnsupportedOperationException("Must be first in history!");
    }

    @Override
    public void save(OutputStream stream) throws IOException {
        stream.write(Utils.intToByteArray(baseImage.size()));
        for (byte[] layer : baseImage)
            stream.write(layer);
    }

    @Override
    public void load(InputStream stream) throws IOException {
        byte[] buffer = new byte[4];
        stream.read(buffer);
        int sz = Utils.byteArrayToInt(buffer);
        baseImage.clear();
        for (int i = 0; i < sz; i++) {
            buffer = new byte[size.x * size.y];
            stream.read(buffer);
            baseImage.add(buffer);
        }
    }
}
