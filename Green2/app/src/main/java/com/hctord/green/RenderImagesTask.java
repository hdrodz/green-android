package com.hctord.green;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.hctord.green.document.ImageRenderer;
import com.hctord.green.document.PixelArt;
import com.hctord.green.document.PixelArtHandle;

import java.io.FileInputStream;
import java.io.IOException;

/**
* Created by HéctorD on 11/1/2014.
*/
class RenderImagesTask extends AsyncTask<PixelArtHandle, Bitmap, Void> {
    private OnBitmapGeneratedListener listener;
    private Context context;
    private int position = 0;

    public RenderImagesTask(Context context) {
        this.context = context;
    }

    public void setOnUpdateListener(OnBitmapGeneratedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(PixelArtHandle... handles) {
        ImageRenderer renderer = new ImageRenderer();

        for (PixelArtHandle handle : handles) {
            PixelArt art;
            try {
                FileInputStream fis = context.openFileInput(handle.getFilename());
                art = new PixelArt(fis);
                fis.close();
            }
            catch (IOException e) {
                continue;
            }
            renderer.switchSource(art);
            publishProgress(renderer.copyCache());
        }
        return null;
    }

    @Override
    public void onProgressUpdate(Bitmap... progress) {
        if (listener != null)
            for (Bitmap bmp : progress)
                listener.onBitmapGenerated(bmp, position++);
    }

    public static interface OnBitmapGeneratedListener {
        public void onBitmapGenerated(Bitmap generatedBitmap, int position);
    }
}
