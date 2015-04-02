package com.hctord.green.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hctord.green.R;
import com.hctord.green.document.DocumentManager;
import com.hctord.green.document.ImageRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* Created by HéctorD on 11/28/2014.
*/
public class OpenDocumentAdapter extends RecyclerView.Adapter<OpenDocumentAdapter.ViewHolder> {

    private List<DocumentManager.OpenPixelArtInfo> openDocuments;
    private List<BitmapDrawable> renderedDocuments;

    public OpenDocumentAdapter(final List<DocumentManager.OpenPixelArtInfo> openDocuments) {
        this.openDocuments = openDocuments;

        renderedDocuments = new ArrayList<BitmapDrawable>();
        new RenderDocumentsTask().execute();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FrameLayout layout = (FrameLayout)
                LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cell_open_document, parent, false);

        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DocumentManager.OpenPixelArtInfo info = openDocuments.get(position);
        holder.filename.setText(info.getFilename().replace(".green", ""));
        holder.thumbnail.setImageDrawable(renderedDocuments.get(position));
    }

    @Override
    public int getItemCount() {
        return openDocuments.size();
    }

    public void documentAdded() {
        new RenderDocumentsTask().execute(openDocuments.size() - 1);
    }

    public void documentChanged(int index) {
        new RenderDocumentsTask().execute(index, index + 1);
    }

    public void documentClosed(int index) {
        for (int i = index; i < renderedDocuments.size(); ++i) {
            renderedDocuments.remove(index);
        }
        new RenderDocumentsTask().execute(index);
    }

    private class RenderDocumentsTask extends AsyncTask<Integer, BitmapDrawable, Void> {
        private ImageRenderer renderer = new ImageRenderer();
        @Override
        protected Void doInBackground(Integer... params) {
            DocumentManager documentManager = DocumentManager.getDocumentManager();
            int start, end;
            switch (params.length) {
                case 0:
                    start = 0;
                    end = openDocuments.size();
                    break;
                case 1:
                    start = params[0];
                    end = openDocuments.size();
                    break;
                default:
                    start = params[0];
                    end = params[1];
                    break;
            }

            for (int i = start; i < end; ++i) {
                DocumentManager.OpenPixelArtInfo info = documentManager.getOpenDocumentInfoList().get(i);
                renderer.switchSource(documentManager.getDocument(info));
                BitmapDrawable bd = new BitmapDrawable(
                        documentManager.getContext().getResources(),
                        renderer.copyCache());
                bd.setAntiAlias(false);
                bd.setFilterBitmap(false);
                bd.setDither(false);
                publishProgress(bd);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(BitmapDrawable... progress) {
            Collections.addAll(renderedDocuments, progress);
            notifyDataSetChanged();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView root;
        public ImageView thumbnail;
        public TextView filename;
        public ImageButton close;

        public ViewHolder(FrameLayout cv) {
            super(cv);
            root = (CardView) cv.findViewById(R.id.card);
            thumbnail = (ImageView) cv.findViewById(R.id.thumbnail);
            filename = (TextView) cv.findViewById(R.id.name);
            close = (ImageButton) cv.findViewById(R.id.close);
        }
    }
}
