package com.hctord.green.document.history;

import android.util.SparseArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.hctord.green.document.PixelArt;

/**
 * Stores image history data.
 */
public interface HistoryEntry {
    public static final SparseArray<Class> CLASS_MAP = new SparseArray<Class>();
    public int getId();
    public void init(Object...args);
    public void undo(PixelArt art);
    public void redo(PixelArt art);
    public void save(OutputStream stream) throws IOException;
    public void load(InputStream stream) throws IOException;
}
