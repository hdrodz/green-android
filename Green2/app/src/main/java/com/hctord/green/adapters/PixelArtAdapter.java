package com.hctord.green.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hctord.green.R;
import com.hctord.green.document.ImageRenderer;
import com.hctord.green.document.PixelArt;
import com.hctord.green.util.Action;
import com.hctord.green.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * All I believe and all I know are being taken from me can't get home
 */
public class PixelArtAdapter extends BaseAdapter {

    private static final View.OnClickListener MORE_BUTTON_LISTENER = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            PopupMenu menu = (PopupMenu)view.getTag();
            menu.show();
        }
    };

    private final View.OnClickListener SHARE_BUTTON_LISTENER = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int position = (Integer)view.getTag();
            if (callbacks != null)
                callbacks.onRequestShare(position);
        }
    };
    private final View.OnClickListener PREVIEW_BUTTON_LISTENER = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (callbacks != null) {
                Pair<Integer, ImageView> tag = (Pair<Integer, ImageView>)view.getTag();
                if (tag != null)
                    callbacks.onRequestPreview(tag.first, tag.second);
            }
        }
    };
    private final View.OnClickListener THUMBNAIL_LISTENER = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int position = (Integer)view.getTag();
            if (callbacks != null)
                callbacks.onRequestOpen(position);
        }
    };

    private Context context;
    private Callbacks callbacks;
    private List<PixelArtHandle2> files;

    public PixelArtAdapter(
            final Context context,
            final Action<PixelArtHandle2> progressUpdateCallback,
            final Action<Void> progressFinishCallback,
            Callbacks generalCallbacks
    ) {
        final AsyncTask<Void, PixelArtHandle2, Void> scanFilesTask = new AsyncTask<Void, PixelArtHandle2, Void>() {

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
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                    renderer.switchSource(pixelArt);
                    renderedBitmap = renderer.copyCache();
                    renderedAsDrawable = new BitmapDrawable(context.getResources(), renderedBitmap);
                    renderedAsDrawable.setDither(false);
                    renderedAsDrawable.setAntiAlias(false);
                    renderedAsDrawable.setFilterBitmap(false);
                    PixelArtHandle2 handle = new PixelArtHandle2();
                    handle.filename = filename;
                    handle.preview = renderedBitmap;
                    handle.previewAsDrawable = renderedAsDrawable;
                    handle.mostFrequentColor = pixelArt.getMostUsedColor();
                    handle.averageColor = pixelArt.getAverageColor();
                    handle.averageColorSat = Utils.maxSaturation(handle.averageColor);
                    float lum = Utils.getLuminance(handle.averageColorSat);
                    if (lum > 0.5f) {
                        handle.textColor = 0xFF101010;
                    }
                    else {
                        handle.textColor = 0xFFFFFFFF;
                    }
                    publishProgress(handle);
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(PixelArtHandle2...progress) {
                for (PixelArtHandle2 handle : progress) {
                    files.add(handle);
                    notifyDataSetChanged();
                    if (progressUpdateCallback != null)
                        progressUpdateCallback.run(handle);
                }
            }

            @Override
            protected void onPostExecute(Void unused) {
                if (progressFinishCallback != null)
                    progressFinishCallback.run(null);
            }
        };

        this.context = context;
        files = new ArrayList<>();
        scanFilesTask.execute((Void)null);
        callbacks = generalCallbacks;
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public PixelArtHandle2 getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.cell_document, parent, false);

            vh = new ViewHolder();

            vh.cardView = (CardView)convertView.findViewById(R.id.card);
            vh.thumbnail = (ImageView)convertView.findViewById(R.id.thumbnail);
            vh.filename = (TextView)convertView.findViewById(R.id.name);
            vh.preview = (ImageButton)convertView.findViewById(R.id.preview);
            vh.share = (ImageButton)convertView.findViewById(R.id.share);
            vh.more = (ImageButton)convertView.findViewById(R.id.more);

            PopupMenu menu = new PopupMenu(context, vh.more);
            menu.inflate(R.menu.context_document);
            menu.setOnMenuItemClickListener(new MoreMenuListener(position));
            vh.more.setTag(menu);
            vh.more.setOnClickListener(MORE_BUTTON_LISTENER);

            vh.share.setTag(position);
            vh.share.setOnClickListener(SHARE_BUTTON_LISTENER);

            vh.preview.setTag(new Pair<>(position, vh.thumbnail));
            vh.preview.setOnClickListener(PREVIEW_BUTTON_LISTENER);

            vh.thumbnail.setTag(position);
            vh.thumbnail.setOnClickListener(THUMBNAIL_LISTENER);

            convertView.setTag(vh);
        }
        else {
            vh = (ViewHolder)convertView.getTag();
        }

        PixelArtHandle2 handle = files.get(position);

        // Temporary fix for bug in CardView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            vh.cardView.setBackgroundColor(handle.averageColorSat);
            vh.filename.setTextColor(handle.textColor);
            vh.more.setColorFilter(handle.textColor);
            vh.share.setColorFilter(handle.textColor);
            vh.preview.setColorFilter(handle.textColor);
        }

        vh.thumbnail.setImageDrawable(handle.previewAsDrawable);
        vh.filename.setText(handle.filename.substring(0, handle.filename.length() - ".green".length()));

        return convertView;
    }

    ////////////////////////////////////////////////////////////////////////
    //
    // Inner classes
    //
    ////////////////////////////////////////////////////////////////////////

    private class MoreMenuListener implements PopupMenu.OnMenuItemClickListener {
        private int position;

        public MoreMenuListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (callbacks == null)
                return false;

            switch (menuItem.getItemId()) {
                case R.id.action_delete:
                    callbacks.onRequestDelete(position);
                    break;
                case R.id.action_export:
                    callbacks.onRequestExport(position);
                    break;
                case R.id.action_rename:
                    callbacks.onRequestRename(position);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    public static interface Callbacks {
        public void onRequestShare(int position);
        public void onRequestRename(int position);
        public void onRequestExport(int position);
        public void onRequestDelete(int position);
        public void onRequestPreview(int position, View caller);
        public void onRequestOpen(int position);
    }

    public static class PixelArtHandle2 implements Parcelable {
        private String filename;
        private Bitmap preview;
        private BitmapDrawable previewAsDrawable;
        private int mostFrequentColor;
        private int averageColor;
        private int averageColorSat;
        private int textColor;

        private PixelArtHandle2() {}

        private PixelArtHandle2(Parcel in) {
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

        public static final Creator<PixelArtHandle2> CREATOR = new Creator<PixelArtHandle2>() {
            @Override
            public PixelArtHandle2 createFromParcel(Parcel parcel) {
                return new PixelArtHandle2(parcel);
            }

            @Override
            public PixelArtHandle2[] newArray(int len) {
                return new PixelArtHandle2[len];
            }
        };
    }

    private static class ViewHolder {
        public CardView cardView;
        public ImageView thumbnail;
        public TextView filename;
        public ImageButton preview;
        public ImageButton share;
        public ImageButton more;
    }
}
