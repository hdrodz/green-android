package com.hctord.green.document.history;

import android.util.SparseArray;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.List;

import com.hctord.green.document.PixelArt;

/**
 * Manages edit history of an image.
 */
public class HistoryManager {
    public static final int MAX_HISTORY_STEPS = 128;

    private SparseArray<List<byte[]>> history;
    private SparseIntArray historySteps;
    private PixelArt src;

    public HistoryManager(PixelArt source, int layer) {
        history = new SparseArray<List<byte[]>>();
        historySteps = new SparseIntArray();
        int layerCount = source.getFrames().size();
        for (int i = 0; i < layerCount; i++) {
            history.put(i, new ArrayList<byte[]>(MAX_HISTORY_STEPS));
            history.get(i).add(source.getLayer(i).clone());
        }
        src = source;
    }

    public void saveToHistory(int layer) {
        List<byte[]> layerHistory = history.get(layer);
        int currentStep = historySteps.get(layer);
        while (layerHistory.size() > currentStep + 1) {
            layerHistory.remove(currentStep);
        }
        layerHistory.add(src.getLayer(layer).clone());
        historySteps.put(layer, currentStep + 1);
    }

    public boolean canUndo(int layer) {
        return historySteps.get(layer) > 0;
    }

    public boolean canRedo(int layer) {
        int histSize = history.get(layer).size();
        return historySteps.get(layer) < histSize - 1;
    }

    public void undo(int layer) {
        int step = historySteps.get(layer);
        historySteps.put(layer, step + 1);
    }

    public void redo(int layer) {
        int step = historySteps.get(layer);
        historySteps.put(layer, step - 1);
    }
}
