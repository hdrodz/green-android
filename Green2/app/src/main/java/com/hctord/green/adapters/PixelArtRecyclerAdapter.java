package com.hctord.green.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hctord.green.R;
import com.hctord.green.document.FileScanner;
import com.hctord.green.util.Action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HéctorD on 6/11/2015.
 */
public class PixelArtRecyclerAdapter
    extends RecyclerView.Adapter<PixelArtRecyclerAdapter.ViewHolder> {

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

    private Callbacks callbacks;
    private List<FileScanner.PixelArtHandle> files;

    public PixelArtRecyclerAdapter(
            final Context context,
            final Action<FileScanner.PixelArtHandle> progressUpdateCallback,
            final Action<Void> progressFinishCallback,
            Callbacks callbacks
    ) {
        this.files = new ArrayList<>();
        this.callbacks = callbacks;

        FileScanner.scan(
                context,
                new Action<FileScanner.PixelArtHandle>() {
                    @Override
                    public void run(FileScanner.PixelArtHandle parameter) {
                        files.add(parameter);
                        notifyDataSetChanged();
                        if (progressUpdateCallback != null)
                            progressUpdateCallback.run(parameter);
                    }
                },
                new Action<Void>() {
                    @Override
                    public void run(Void parameter) {
                        if (progressFinishCallback != null)
                            progressFinishCallback.run(parameter);
                    }
                }
        );
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.cell_document, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        PopupMenu menu = new PopupMenu(vh.more.getContext(), vh.more);
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

        FileScanner.PixelArtHandle handle = files.get(position);

        // Temporary fix for bug in CardView
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        vh.cardView.setCardBackgroundColor(handle.getAverageColorSat());
        vh.filename.setTextColor(handle.getTextColor());
        vh.more.setColorFilter(handle.getTextColor());
        vh.share.setColorFilter(handle.getTextColor());
        vh.preview.setColorFilter(handle.getTextColor());
        //}

        vh.thumbnail.setImageDrawable(handle.getPreviewAsDrawable(vh.thumbnail.getContext()));
        vh.filename.setText(handle.getFilename().substring(0, handle.getFilename().length() - ".green".length()));

    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public FileScanner.PixelArtHandle getItem(int position) {
        return files.get(position);
    }

    public void refreshFilesList(Context context) {
        files.clear();
        notifyDataSetChanged();

        FileScanner.scan(
                context,
                new Action<FileScanner.PixelArtHandle>() {
                    @Override
                    public void run(FileScanner.PixelArtHandle parameter) {
                        files.add(parameter);
                        notifyDataSetChanged();
                    }
                },
                null
        );
    }

    ////////////////////////////////////////////////////////////////////////
    //
    // Inner classes
    //
    ////////////////////////////////////////////////////////////////////////

    public interface Callbacks {
        void onRequestShare(int position);
        void onRequestRename(int position);
        void onRequestExport(int position);
        void onRequestDelete(int position);
        void onRequestPreview(int position, View caller);
        void onRequestOpen(int position);
    }

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

    /**
     * View holder class
     */
    public static class ViewHolder
        extends RecyclerView.ViewHolder {
        public CardView cardView;
        public ImageView thumbnail;
        public TextView filename;
        public ImageButton preview;
        public ImageButton share;
        public ImageButton more;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.card);
            thumbnail = (ImageView)itemView.findViewById(R.id.thumbnail);
            filename = (TextView)itemView.findViewById(R.id.name);
            preview = (ImageButton)itemView.findViewById(R.id.preview);
            share = (ImageButton)itemView.findViewById(R.id.share);
            more = (ImageButton)itemView.findViewById(R.id.more);
        }
    }
}
