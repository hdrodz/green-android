package com.hctord.green.adapters;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hctord.green.R;
import com.hctord.green.document.ImageRenderer;
import com.hctord.green.document.PixelArt;
import com.hctord.green.widget.CheckableLinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rodri on 9/24/2016.
 */

public class FrameRecyclerAdapter
        extends RecyclerView.Adapter<FrameRecyclerAdapter.ViewHolder> {

    private final View.OnClickListener ITEM_SELECT_LISTENER = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            select((Integer)v.getTag(), true);
        }
    };

    private final View.OnLongClickListener ITEM_LONG_CLICK_LISTENER = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    };

    private Context context;
    private PixelArt pixelArt;
    private ImageRenderer renderer;
    private List<BitmapDrawable> rendererCache;
    private List<Boolean> visibleLayers;

    private boolean isInitialized;
    private int prelimPosition = 0;
    private CheckableLinearLayout lastSelection;
    private SparseArray<CheckableLinearLayout> views;
    private Callbacks callbacks;

    public FrameRecyclerAdapter(Context context, PixelArt pixelArt) {
        this.context = context;
        this.pixelArt = pixelArt;

        rendererCache = new ArrayList<>();
        renderer = new ImageRenderer();
        renderer.switchSource(pixelArt);

        visibleLayers = new ArrayList<>();

        views = new SparseArray<>(pixelArt.getFrames().size());

        for (int i = 0; i < pixelArt.getFrames().size(); ++i) {
            renderer.updateCache(i);
            BitmapDrawable bd = new BitmapDrawable(context.getResources(), renderer.copyCache());
            bd.setDither(false);
            bd.setFilterBitmap(false);
            bd.setAntiAlias(false);
            rendererCache.add(bd);
            visibleLayers.add(true);
        }
    }

    public void setListener(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void select(int pos) {
        select(pos, false);
    }

    public void select(int pos, boolean triggerCallback) {
        if (!isInitialized) {
            prelimPosition = pos;
        }
        else {
            if (lastSelection != null)
                lastSelection.setChecked(false);
            lastSelection = views.get(pos);
            lastSelection.setChecked(true);
            if (triggerCallback && callbacks != null)
                callbacks.onSelectFrame(pos);
        }
    }

    @Override
    public FrameRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                    .inflate(R.layout.row_layer, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FrameRecyclerAdapter.ViewHolder holder, int position) {
        holder.preview.setImageDrawable(rendererCache.get(position));
        holder.container.setTag(position);
        holder.container.setOnClickListener(ITEM_SELECT_LISTENER);
        if (!isInitialized && prelimPosition == position) {
            holder.container.setChecked(true);
            lastSelection = holder.container;
            isInitialized = true;
        }
        views.put(position, holder.container);
    }

    @Override
    public int getItemCount() {
        return pixelArt.getFrames().size();
    }

    public void invalidateFrame(int frame) {
        renderer.updateCache(frame);
        BitmapDrawable bd = new BitmapDrawable(context.getResources(), renderer.copyCache());
        bd.setDither(false);
        bd.setFilterBitmap(false);
        bd.setAntiAlias(false);
        rendererCache.set(frame, bd);
        notifyDataSetChanged();
    }

    public void invalidateFrames() {
        rendererCache.clear();

        for (int i = 0; i < pixelArt.getFrames().size(); ++i) {
            renderer.updateCache(i);
            BitmapDrawable bd = new BitmapDrawable(context.getResources(), renderer.copyCache());
            bd.setDither(false);
            bd.setFilterBitmap(false);
            bd.setAntiAlias(false);
            rendererCache.add(bd);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView preview;
        CheckableLinearLayout container;

        ViewHolder(View itemView) {
            super(itemView);
            container = (CheckableLinearLayout)itemView;
            preview = (ImageView)itemView.findViewById(R.id.preview);
        }
    }

    public interface Callbacks {
        void onSelectFrame(int position);
    }
}
