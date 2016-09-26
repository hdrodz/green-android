package com.hctord.green.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.hctord.green.R;
import com.hctord.green.widget.CheckableColorView;

import java.util.List;

/**
 * Adapter for palettes and shit i guess
 */
@Deprecated
public class PaletteAdapter extends BaseAdapter {

    private Context context;
    private List<Integer> palette;

    public PaletteAdapter(Context context, List<Integer> palette) {
        this.palette = palette;
        this.context = context;
    }

    @Override
    public int getCount() {
        return palette.size();
    }

    @Override
    public Integer getItem(int i) {
        return palette.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup root) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.cell_color, root, false);
        }

        CheckableColorView cv = (CheckableColorView)convertView;
        cv.setColor(palette.get(position));

        return convertView;
    }
}
