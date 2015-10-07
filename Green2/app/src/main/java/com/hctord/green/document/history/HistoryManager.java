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

    public HistoryManager(PixelArt source) {
        history = new SparseArray<>();
        historySteps = new SparseIntArray();
        int frameCount = source.getFrames().size();
        for (int i = 0; i < frameCount; i++) {
            history.put(i, new ArrayList<byte[]>(MAX_HISTORY_STEPS));
            history.get(i).add(source.getFrame(i).clone());
        }
        src = source;
    }

    public void saveToHistory(int frame) {
        List<byte[]> frameHistory = history.get(frame);
        int currentStep = historySteps.get(frame);
        while (frameHistory.size() > currentStep + 1) {
            frameHistory.remove(currentStep);
        }
        frameHistory.add(src.getFrame(frame).clone());
        historySteps.put(frame, currentStep + 1);
    }

    public boolean canUndo(int frame) {
        return historySteps.get(frame) > 0;
    }

    public boolean canRedo(int frame) {
        int histSize = history.get(frame).size();
        return historySteps.get(frame) < histSize - 1;
    }

    public void undo(int frame) {
        int step = historySteps.get(frame);
        historySteps.put(frame, step + 1);
    }

    public void redo(int frame) {
        int step = historySteps.get(frame);
        historySteps.put(frame, step - 1);
    }
}
