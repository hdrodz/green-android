package com.hctord.green.adapters;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.hctord.green.R;
import com.hctord.green.document.ImageRenderer;
import com.hctord.green.document.LayerViewMode;
import com.hctord.green.document.PixelArt;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for layers n shiiiiiii
 */
public class LayerAdapter extends BaseAdapter {

    private Context context;
    private PixelArt pixelArt;
    private ImageRenderer renderer;
    private List<BitmapDrawable> rendererCache;
    private List<Boolean> visibleLayers;
    private int viewMode;

    public LayerAdapter(Context context, PixelArt pixelArt) {
        this.context = context;
        this.pixelArt = pixelArt;

        rendererCache = new ArrayList<BitmapDrawable>();
        renderer = new ImageRenderer();
        renderer.switchSource(pixelArt);

        visibleLayers = new ArrayList<Boolean>();

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

    public void setViewMode(int viewMode) {
        this.viewMode = viewMode;
        notifyDataSetChanged();
    }

    public void setLayerVisible(int layer, boolean visible) {
        visibleLayers.set(layer, visible);
        notifyDataSetChanged();
    }

    public void invalidateLayer(int layer) {
        renderer.updateCache(layer);
        BitmapDrawable bd = new BitmapDrawable(context.getResources(), renderer.copyCache());
        bd.setDither(false);
        bd.setFilterBitmap(false);
        bd.setAntiAlias(false);
        rendererCache.set(layer, bd);
        notifyDataSetChanged();
    }

    public void invalidateLayers() {
        rendererCache.clear();

        for (int i = 0; i < pixelArt.getFrames().size(); ++i) {
            renderer.updateCache(i);
            BitmapDrawable bd = new BitmapDrawable(context.getResources(), renderer.copyCache());
            bd.setDither(false);
            bd.setFilterBitmap(false);
            bd.setAntiAlias(false);
            rendererCache.add(bd);
            if (i > visibleLayers.size())
                visibleLayers.add(true);
        }
    }

    @Override
    public int getCount() {
        return pixelArt.getFrames().size();
    }

    @Override
    public byte[] getItem(int position) {
        return pixelArt.getLayer(position);
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
            vh.layerName = (TextView)convertView.findViewById(R.id.layer_name);
            vh.visible = (CheckBox)convertView.findViewById(R.id.visible);

            convertView.setTag(vh);
        }
        else {
            vh = (ViewHolder)convertView.getTag();
        }

        vh.preview.setImageDrawable(rendererCache.get(position));

        vh.layerName.setText("Layer " + Integer.toString(position + 1));

        vh.visible.setChecked(visibleLayers.get(position));
        vh.visible.setEnabled(viewMode == LayerViewMode.ALL_SELECTED);

        return convertView;
    }

    private static class ViewHolder {
        public ImageView preview;
        public TextView layerName;
        public CheckBox visible;
    }
}
