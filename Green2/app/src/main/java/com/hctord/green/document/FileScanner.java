package com.hctord.green.document;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import com.hctord.green.util.Action;
import com.hctord.green.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Created by HéctorD on 6/11/2015.
 */
public final class FileScanner {
    private FileScanner() {}

    public static void scan(
            final Context context,
            final Action<PixelArtHandle> progressUpdateCallback,
            final Action<Void> progressFinishCallback
    ) {
        new ScanFilesTask(context, progressUpdateCallback, progressFinishCallback).doInBackground(
                (Void)null
        );
    }

    private static class ScanFilesTask extends AsyncTask<Void, PixelArtHandle, Void> {
        private Action<PixelArtHandle> progressUpdateCallback;
        private Action<Void> progressFinishCallback;
        private Context context;

        public ScanFilesTask(
                final Context context,
                final Action<PixelArtHandle> progressUpdateCallback,
                final Action<Void> progressFinishCallback) {
            this.context = context;
            this.progressUpdateCallback = progressUpdateCallback;
            this.progressFinishCallback = progressFinishCallback;
        }

        @Override
        protected Void doInBackground(Void... unused) {
            ImageRenderer renderer = new ImageRenderer();
            File artDir = context.getFilesDir();
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return !filename.startsWith("$") && filename.endsWith(".green");
                }
            };
            File[] art = artDir.listFiles(filter);

            for (File artFile : art) {
                String filename = artFile.getName();
                Bitmap renderedBitmap;
                BitmapDrawable renderedAsDrawable;
                PixelArt pixelArt;
                try {
                    FileInputStream fis = new FileInputStream(artFile);
                    pixelArt = new PixelArt(fis);
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                renderer.switchSource(pixelArt);
                renderedBitmap = renderer.copyCache();
                renderedAsDrawable = new BitmapDrawable(context.getResources(), renderedBitmap);
                renderedAsDrawable.setDither(false);
                renderedAsDrawable.setAntiAlias(false);
                renderedAsDrawable.setFilterBitmap(false);
                PixelArtHandle handle = new PixelArtHandle();
                handle.filename = filename;
                handle.preview = renderedBitmap;
                handle.previewAsDrawable = renderedAsDrawable;
                handle.mostFrequentColor = pixelArt.getMostUsedColor();
                handle.averageColor = pixelArt.getAverageColor();
                handle.averageColorSat = Utils.maxSaturation(handle.averageColor);
                float lum = Utils.getLuminance(handle.averageColorSat);
                if (lum > 0.5f) {
                    handle.textColor = 0xFF101010;
                } else {
                    handle.textColor = 0xFFFFFFFF;
                }
                publishProgress(handle);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(PixelArtHandle... progress) {
            for (PixelArtHandle handle : progress) {
                if (progressUpdateCallback != null)
                    progressUpdateCallback.run(handle);
            }
        }

        @Override
        protected void onPostExecute(Void unused) {
            if (progressFinishCallback != null)
                progressFinishCallback.run(null);
        }
    }

    public static class PixelArtHandle implements Parcelable {
        private String filename;
        private Bitmap preview;
        private BitmapDrawable previewAsDrawable;
        private int mostFrequentColor;
        private int averageColor;
        private int averageColorSat;
        private int textColor;

        private PixelArtHandle() {}

        private PixelArtHandle(Parcel in) {
            filename = in.readString();
            int len = in.readInt();
            byte[] image = new byte[len];
            in.readByteArray(image);
            mostFrequentColor = in.readInt();
            averageColor = in.readInt();
            averageColorSat = in.readInt();
            textColor = in.readInt();

            preview = BitmapFactory.decodeByteArray(image, 0, image.length);
        }

        public BitmapDrawable getPreviewAsDrawable(Context context) {
            if (previewAsDrawable == null) {
                previewAsDrawable = new BitmapDrawable(context.getResources(), preview);
                previewAsDrawable.setDither(false);
                previewAsDrawable.setFilterBitmap(false);
                previewAsDrawable.setAntiAlias(false);
            }
            return previewAsDrawable;
        }

        public String getFilename() {
            return filename;
        }

        public int getAverageColor() {
            return averageColor;
        }

        public int getAverageColorSat() {
            return averageColorSat;
        }

        public int getMostFrequentColor() {
            return mostFrequentColor;
        }

        public int getTextColor() {
            return textColor;
        }

        public Bitmap getPreview() {
            return preview;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int options) {
            out.writeString(filename);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            preview.compress(Bitmap.CompressFormat.PNG, 100, baos);
            try {
                baos.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            byte[] compressedImage = baos.toByteArray();
            out.writeInt(compressedImage.length);
            out.writeByteArray(compressedImage);
            out.writeInt(mostFrequentColor);
            out.writeInt(averageColor);
            out.writeInt(averageColorSat);
            out.writeInt(textColor);
        }

        public static final Creator<PixelArtHandle> CREATOR = new Creator<PixelArtHandle>() {
            @Override
            public PixelArtHandle createFromParcel(Parcel parcel) {
                return new PixelArtHandle(parcel);
            }

            @Override
            public PixelArtHandle[] newArray(int len) {
                return new PixelArtHandle[len];
            }
        };
    }
}
