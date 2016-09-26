package com.hctord.green.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hctord.green.R;
import com.hctord.green.widget.CheckableColorView;

import java.util.List;

/**
 * Created by rodri on 9/24/2016.
 */
public class PaletteRecyclerAdapter extends RecyclerView.Adapter<PaletteRecyclerAdapter.ViewHolder> {

    private final View.OnClickListener ITEM_SELECT_LISTENER = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            select((Integer)v.getTag(), true);
        }
    };

    private final View.OnLongClickListener ITEM_LONG_CLICK_LISTENER = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (callbacks != null)
                callbacks.onLongTapColor((Integer)v.getTag(), v);
            return true;
        }
    };

    private boolean isInitialized = false;
    private int prelimPosition = 0;
    private CheckableColorView lastSelection = null;
    private Callbacks callbacks = null;
    private List<Integer> palette;
    private SparseArray<CheckableColorView> views;

    public PaletteRecyclerAdapter(List<Integer> palette) {
        this.palette = palette;
        views = new SparseArray<>(palette.size());
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
                callbacks.onSelectColor(pos);
        }
    }

    public void setListener(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View  v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cell_color, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.cv.setColor(palette.get(position));
        holder.cv.setTag(position);
        holder.cv.setOnClickListener(ITEM_SELECT_LISTENER);
        holder.cv.setOnLongClickListener(ITEM_LONG_CLICK_LISTENER);
        if (!isInitialized && position == prelimPosition) {
            holder.cv.setChecked(true);
            lastSelection = holder.cv;
            isInitialized = true;
        }
        views.put(position, holder.cv);
    }

    @Override
    public int getItemCount() {
        return palette.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CheckableColorView cv;

        ViewHolder(View itemView) {
            super(itemView);
            cv = (CheckableColorView)itemView;
        }
    }

    public interface Callbacks {
        void onSelectColor(int position);
        void onLongTapColor(int position, View anchor);
    }
}
