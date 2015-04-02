package com.hctord.green.widget.toolbox;

import com.hctord.green.widget.PixelEditorView2;

/**
 * Basically Freeform but defaulting to color 0 which is the background color.
 */
public final class Eraser extends Freeform {

    public Eraser(PixelEditorView2 editor, int layer) {
        super(editor, layer, (byte)0);
    }
}
