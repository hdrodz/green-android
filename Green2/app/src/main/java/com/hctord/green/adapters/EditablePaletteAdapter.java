package com.hctord.green.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;

import com.hctord.green.R;
import com.hctord.green.widget.CheckableColorView;

import java.util.List;

/**
 * Created by HéctorD on 11/9/2014.
 */
public class EditablePaletteAdapter extends BaseAdapter {

    private Context context;
    private List<Integer> palette;

    public EditablePaletteAdapter(Context context, List<Integer> palette) {
        this.palette = palette;
        this.context = context;
    }

    @Override
    public int getCount() {
        return palette.size();
    }

    @Override
    public Object getItem(int i) {
        return palette.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup root) {
        ViewHolder vh;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.row_color, root, false);

            vh = new ViewHolder();
            vh.colorPreview = convertView.findViewById(R.id.color);
            vh.editButton = (ImageButton)convertView.findViewById(R.id.edit);

            convertView.setTag(vh);
        }
        else {
            vh = (ViewHolder)convertView.getTag();
        }

        vh.colorPreview.setBackgroundColor(palette.get(position));

        return convertView;
    }

    private static class ViewHolder {
        public View colorPreview;
        public ImageButton editButton;
    }

}
