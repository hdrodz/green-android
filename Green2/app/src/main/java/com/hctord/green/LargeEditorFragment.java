package com.hctord.green;

import android.content.res.Resources;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.hctord.green.adapters.FrameAdapter;
import com.hctord.green.adapters.PaletteAdapter;
import com.hctord.green.util.ColorPickerPopupWindow;
import com.hctord.green.widget.PixelEditorView2;

/**
 * Fragment that manages the editor on a large sized screen (w > 720dp)
 */
public class LargeEditorFragment
        extends CommonEditorFragment
        implements
            ColorPickerPopupWindow.OnColorChangedListener {

    private static final String STATE_IS_FULLSCREEN = "com.hctord.green.STATE_IS_FULLSCREEN";

    private final View.OnClickListener TOOLBOX_BUTTON_LISTENER = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LargeEditorFragment.super.setToolFromId(view.getId());
            lastClickedButton.setColorFilter(0xFF000000);
            lastClickedButton = (ImageButton) view;
            lastClickedButton.setColorFilter(0xFF008000);
        }
    };
    private final View.OnClickListener ADD_COLOR_BUTTON_LISTENER = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Set the modifying color to an invalid index so that we know that it is going to add
            // a new color when 'ok' is pressed.
            modifyingColor = -1;
            colorPopupWindow.showAsDropDown(view);
        }
    };

    private PixelEditorView2 editorView;
    private PaletteAdapter paletteAdapter;
    private FrameAdapter frameAdapter;
    private ListView paletteList;
    private ListView framesList;
    private View layersPanel, palettePanel, toolboxPanel;
    private ImageButton lastClickedButton;
    private Animation layersFlyOutAnimation;
    private Animation layersFlyInAnimation;
    private Animation paletteFlyOutAnimation;
    private Animation paletteFlyInAnimation;
    private Animation toolboxFlyOutAnimation;
    private Animation toolboxFlyInAnimation;

    private RelativeLayout.LayoutParams fullscreenPalettePanelLayoutParams;
    private RelativeLayout.LayoutParams palettePanelLayoutParams;
    private AnimatedVectorDrawable goFullscreenAnimatedDrawable;
    private AnimatedVectorDrawable exitFullscreenAnimatedDrawable;
    private Drawable goFullscreenDrawable;
    private Drawable exitFullscreenDrawable;

    private ColorPickerPopupWindow colorPopupWindow;
    private int colorPickerOffset;
    private int modifyingColor;
    private boolean isFullscreen = false;

    public LargeEditorFragment() {
        super(R.layout.fragment_editor_large);
    }

    @Override
    protected void init(View root, Bundle savedInstanceState) {
        Resources res = getResources();
        colorPickerOffset = (int) res.getDimension(R.dimen.color_picker_offset);
        editorView = getEditorView();

        layersFlyOutAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_layers_out);
        layersFlyOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                layersPanel.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        layersFlyInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_layers_in);

        toolboxFlyOutAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_toolbox_out);
        toolboxFlyOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                toolboxPanel.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        toolboxFlyInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_toolbox_in);

        paletteFlyOutAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_palette_out);

        paletteFlyInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_palette_in);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            goFullscreenAnimatedDrawable = (AnimatedVectorDrawable) getActivity().getDrawable(R.drawable.ic_action_fullscreen_vector_animated);
            exitFullscreenAnimatedDrawable = (AnimatedVectorDrawable) getActivity().getDrawable(R.drawable.ic_action_fullscreen_vector_animated_inv);
        } else {
            goFullscreenDrawable = getResources().getDrawable(R.drawable.ic_action_fullscreen);
            exitFullscreenDrawable = getResources().getDrawable(R.drawable.ic_action_exit_fullscreen);
        }

        layersPanel = root.findViewById(R.id.frames_panel);
        toolboxPanel = root.findViewById(R.id.toolbox_panel);
        palettePanel = root.findViewById(R.id.palette_panel);

        // Setup toolbox buttons
        ImageButton btn;
        lastClickedButton = btn = (ImageButton) root.findViewById(R.id.action_pencil);
        btn.setOnClickListener(TOOLBOX_BUTTON_LISTENER);
        btn = (ImageButton) root.findViewById(R.id.action_eraser);
        btn.setOnClickListener(TOOLBOX_BUTTON_LISTENER);
        btn = (ImageButton) root.findViewById(R.id.action_bucket);
        btn.setOnClickListener(TOOLBOX_BUTTON_LISTENER);
        btn = (ImageButton) root.findViewById(R.id.action_line);
        btn.setOnClickListener(TOOLBOX_BUTTON_LISTENER);
        btn = (ImageButton) root.findViewById(R.id.action_rect);
        btn.setOnClickListener(TOOLBOX_BUTTON_LISTENER);
        btn = (ImageButton) root.findViewById(R.id.action_ellipse);
        btn.setOnClickListener(TOOLBOX_BUTTON_LISTENER);

        // Setup color list
        paletteList = (ListView) root.findViewById(R.id.palette_list);
        paletteAdapter = new PaletteAdapter(getActivity(), editorView.getTarget().getPalette());
        paletteList.setAdapter(paletteAdapter);
        paletteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                editorView.setBrushColor(position);

            }
        });
        paletteList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                modifyingColor = i;
                colorPopupWindow.setColor(paletteAdapter.getItem(i));
                colorPopupWindow.showAsDropDown(view, colorPickerOffset, -colorPickerOffset);
                return true;
            }
        });
        paletteList.setItemChecked(1, true);

        // Setup color editor popup
        colorPopupWindow = new ColorPickerPopupWindow(getActivity(), (ViewGroup) root);
        colorPopupWindow.setOnColorChangedListener(this);

        // Setup layers list
        framesList = (ListView) root.findViewById(R.id.frames_list);
        frameAdapter = new FrameAdapter(getActivity(), editorView.getTarget());
        framesList.setAdapter(frameAdapter);
        framesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editorView.setEditingFrame(position);
            }
        });
        framesList.setItemChecked(0, true);

        btn = (ImageButton)root.findViewById(R.id.add_frame);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorView.addFrame();
                frameAdapter.notifyDataSetChanged();
            }
        });

        btn = (ImageButton) root.findViewById(R.id.add_color);
        btn.setOnClickListener(ADD_COLOR_BUTTON_LISTENER);

        float m = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -50, getResources().getDisplayMetrics());
        palettePanelLayoutParams = new RelativeLayout.LayoutParams(palettePanel.getLayoutParams());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            fullscreenPalettePanelLayoutParams = new RelativeLayout.LayoutParams(palettePanelLayoutParams);
        else
            fullscreenPalettePanelLayoutParams = new RelativeLayout.LayoutParams((ViewGroup.LayoutParams)palettePanelLayoutParams);
        fullscreenPalettePanelLayoutParams.setMargins((int)m, 0, 0, 0);

        // Restore fullscreen state
        if (savedInstanceState != null) {
            isFullscreen = savedInstanceState.getBoolean(STATE_IS_FULLSCREEN, false);
            if (isFullscreen) {
                layersPanel.setVisibility(View.GONE);
                toolboxPanel.setVisibility(View.GONE);
                palettePanel.setLayoutParams(fullscreenPalettePanelLayoutParams);
            }
        }
    }

    @Override
    public void onMenuInflated(Menu menu)
    {
        if (isFullscreen) {
            MenuItem fullscreenItem = menu.findItem(R.id.action_fullscreen);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fullscreenItem.setIcon(exitFullscreenAnimatedDrawable);
            }
            else {
                fullscreenItem.setIcon(exitFullscreenDrawable);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_IS_FULLSCREEN, isFullscreen);
    }

    @Override
    public void onColorChanged(int newColor) {
        if (modifyingColor >= 0) {
            editorView.getTarget().getPalette().set(modifyingColor, newColor);
            editorView.invalidateRenderCache();
        }
        else {
            editorView.getTarget().getPalette().add(newColor);
        }
        paletteAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPreEdit() {

    }

    @Override
    public void onEdit() {
        super.onEdit();
        frameAdapter.invalidateFrame(editorView.getEditingFrame());
    }

    @Override
    public void toggleFullscreen(MenuItem item) {
        if (isFullscreen) {
            layersPanel.setVisibility(View.VISIBLE);
            toolboxPanel.setVisibility(View.VISIBLE);
            palettePanel.setLayoutParams(palettePanelLayoutParams);
            layersPanel.startAnimation(layersFlyInAnimation);
            toolboxPanel.startAnimation(toolboxFlyInAnimation);
            palettePanel.startAnimation(paletteFlyInAnimation);
        }
        else {
            palettePanel.setLayoutParams(fullscreenPalettePanelLayoutParams);
            layersPanel.startAnimation(layersFlyOutAnimation);
            toolboxPanel.startAnimation(toolboxFlyOutAnimation);
            palettePanel.startAnimation(paletteFlyOutAnimation);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AnimatedVectorDrawable icon;
            if (isFullscreen) {
                icon = exitFullscreenAnimatedDrawable;
            }
            else {
                icon = goFullscreenAnimatedDrawable;
            }
            icon.stop();
            item.setIcon(icon);
            icon.start();
        }
        else {
            if (isFullscreen) {
                item.setIcon(exitFullscreenDrawable);
            }
            else {
                item.setIcon(goFullscreenDrawable);
            }
        }

        isFullscreen = !isFullscreen;
    }
}
