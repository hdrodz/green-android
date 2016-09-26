package com.hctord.green.adapters;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hctord.green.R;
import com.hctord.green.document.ImageRenderer;
import com.hctord.green.document.PixelArt;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for layers n shiiiiiii
 */
@Deprecated
public class FrameAdapter extends BaseAdapter {

    private Context context;
    private PixelArt pixelArt;
    private ImageRenderer renderer;
    private List<BitmapDrawable> rendererCache;
    private List<Boolean> visibleLayers;

    public FrameAdapter(Context context, PixelArt pixelArt) {
        this.context = context;
        this.pixelArt = pixelArt;

        rendererCache = new ArrayList<>();
        renderer = new ImageRenderer();
        renderer.switchSource(pixelArt);

        visibleLayers = new ArrayList<>();

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
            if (i >= visibleLayers.size())
                visibleLayers.add(true);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        invalidateFrames();
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return pixelArt.getFrames().size();
    }

    @Override
    public byte[] getItem(int position) {
        return pixelArt.getFrame(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup root) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.row_layer, root, false);

            vh = new ViewHolder();

            vh.preview = (ImageView)convertView.findViewById(R.id.preview);

            convertView.setTag(vh);
        }
        else {
            vh = (ViewHolder)convertView.getTag();
        }

        vh.preview.setImageDrawable(rendererCache.get(position));

        vh.frameIndicator.setText(Integer.toString(position));

        return convertView;
    }

    private static class ViewHolder {
        public ImageView preview;
        public TextView frameIndicator;
    }
}
