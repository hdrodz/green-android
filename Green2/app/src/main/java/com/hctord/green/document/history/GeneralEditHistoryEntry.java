package com.hctord.green.document.history;

import android.graphics.Point;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.hctord.green.document.PixelArt;

/**
 * History entry when a general edit is made.
 */
public class GeneralEditHistoryEntry implements HistoryEntry {
    private static final int ID = 1;

    private byte[] oldLayer, newLayer;
    private int index;
    private Point size;

    static {
        HistoryEntry.CLASS_MAP.append(ID, GeneralEditHistoryEntry.class);
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public void init(Object... args) {
        if (!(args[0] instanceof PixelArt))
            throw new IllegalArgumentException("First arg must be pixel art");
        boolean firstInit = true;
        if (args[1] instanceof Boolean) {
            firstInit = false;
        }
        PixelArt pa = (PixelArt) args[0];
        size = pa.getSize();
        if (firstInit) {
            newLayer = (byte[]) args[1];
            index = (Integer) args[2];
            oldLayer = pa.getLayer(index).clone();
        }
    }

    @Override
    public void undo(PixelArt art) {
        art.getLayers().set(index, oldLayer);
    }

    @Override
    public void redo(PixelArt art) {
        art.getLayers().set(index, newLayer);
    }

    @Override
    public void save(OutputStream stream) throws IOException {
        stream.write(oldLayer);
        stream.write(newLayer);
    }

    @Override
    public void load(InputStream stream) throws IOException {
        newLayer = new byte[size.x * size.y];
        stream.read(newLayer);
        oldLayer = new byte[size.x * size.y];
        stream.read(oldLayer);
    }
}
