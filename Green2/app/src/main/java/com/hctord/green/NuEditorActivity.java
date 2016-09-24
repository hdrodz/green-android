package com.hctord.green;

import android.app.ActivityManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import com.hctord.green.adapters.FrameAdapter;
import com.hctord.green.adapters.PaletteAdapter;
import com.hctord.green.document.DocumentManager;
import com.hctord.green.util.ColorPickerPopupWindow;
import com.hctord.green.widget.PixelEditorView2;

public class NuEditorActivity
        extends AppCompatActivity
        implements ColorPickerPopupWindow.OnColorChangedListener,
                   PixelEditorView2.OnEditListener {

    public static final String EXTRA_HANDLE = "com.hctord.green.NuEditorActivity.EXTRA_HANDLE";

    public static final String STATE_TOOL_ID = "com.hctord.green.NuEditorActivity.STATE_TOOL_ID";

    private static final View.OnClickListener TOOL_BUTTON_LISTENER = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu menu = (PopupMenu)v.getTag();
            menu.show();
        }
    };

    // UI Elements
    private PixelEditorView2 editorView;
    private PaletteAdapter paletteAdapter;
    private FrameAdapter frameAdapter;
    private RecyclerView paletteList;
    private RecyclerView framesList;
    private ImageButton toolboxButton, paletteButton, framesButton;
    private ImageButton lastToolButton;
    private View layersPanel, palettePanel, toolboxPanel;
    private Toolbar toolbar;

    // Listeners
    private PopupMenu.OnMenuItemClickListener toolboxListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            setButtonIconFromId(item.getItemId());
            setToolFromId(item.getItemId());
            return true;
        }
    };

    // Animations
    private Animation layersFlyOutAnimation;
    private Animation layersFlyInAnimation;
    private Animation paletteFlyOutAnimation;
    private Animation paletteFlyInAnimation;
    private Animation toolboxFlyOutAnimation;
    private Animation toolboxFlyInAnimation;

    // State
    private boolean isNarrow = false;
    private boolean isFullscreen;
    private int toolId;
    private ActivityManager.TaskDescription taskDescription;

    // Utility functions

    /**
     * Sets the toolbox button icon based on what menu id was pressed.
     * @param id menu id that was pressed
     */
    private void setButtonIconFromId(int id) {
        switch (id) {
            case R.id.action_pencil:
                toolboxButton.setImageResource(R.drawable.ic_tool_pencil_white);
                break;
            case R.id.action_eraser:
                toolboxButton.setImageResource(R.drawable.ic_tool_eraser_white);
                break;
            case R.id.action_bucket:
                toolboxButton.setImageResource(R.drawable.ic_tool_bucket_white);
                break;
            case R.id.action_line:
                toolboxButton.setImageResource(R.drawable.ic_tool_line_white);
                break;
            case R.id.action_rect:
                toolboxButton.setImageResource(R.drawable.ic_tool_rect_white);
                break;
            case R.id.action_ellipse:
                toolboxButton.setImageResource(R.drawable.ic_tool_ellipse_white);
                break;
        }
        paletteButton.setEnabled(id != R.id.action_eraser);
    }

    /**
     * Sets the pixel editor tool based on what tool id was pressed.
     * @param id id that was pressed
     */
    private void setToolFromId(int id) {
        switch (id) {
            case R.id.action_pencil:
                editorView.setBrush(PixelEditorView2.BrushType.FREEFORM);
                break;
            case R.id.action_eraser:
                editorView.setBrush(PixelEditorView2.BrushType.ERASER);
                break;
            case R.id.action_bucket:
                editorView.setBrush(PixelEditorView2.BrushType.FILL);
                break;
            case R.id.action_line:
                editorView.setBrush(PixelEditorView2.BrushType.LINE);
                break;
            case R.id.action_rect:
                editorView.setBrush(PixelEditorView2.BrushType.RECT);
                break;
            case R.id.action_ellipse:
                editorView.setBrush(PixelEditorView2.BrushType.ELLIPSE);
                break;
        }
        toolId = id;
    }

    private void setColorFromId(int id) {
        if (lastToolButton != null)
            lastToolButton.setColorFilter(Color.BLACK);
        lastToolButton = (ImageButton)findViewById(id);
        lastToolButton.setColorFilter(Color.GREEN);
    }

    ////////////////////////////////////////////
    //
    // AppCompatActivity overrides
    //
    ///////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nu_editor);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Test if the button bar exists in this view.
        isNarrow = findViewById(R.id.button_bar) != null;


        layersFlyOutAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_layers_out);
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

        layersFlyInAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_layers_in);

        paletteFlyOutAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_palette_out);

        paletteFlyInAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_palette_in);

        layersPanel = findViewById(R.id.frames_panel);
        toolboxPanel = findViewById(R.id.toolbox_panel);
        palettePanel = findViewById(R.id.palette_panel);


        if (isNarrow) {
            // Setup toolbox button
            toolboxButton = (ImageButton)findViewById(R.id.tools_button);
            PopupMenu toolboxMenu = new PopupMenu(this, toolboxButton);
            toolboxMenu.inflate(R.menu.context_tools);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                toolboxButton.setOnTouchListener(toolboxMenu.getDragToOpenListener());
            }
            toolboxButton.setTag(toolboxMenu);
            toolboxButton.setOnClickListener(TOOL_BUTTON_LISTENER);
            toolboxMenu.setOnMenuItemClickListener(toolboxListener);

            // Setup palette button
        }
        else {

        }

        // Load the image
        try {
            Bundle extras = getIntent().getExtras();
            DocumentManager.OpenPixelArtInfo handle = extras.getParcelable(EXTRA_HANDLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                ;
            editorView.setOnEditListener(this);
            editorView.setTarget(DocumentManager.getDocumentManager().getDocument(handle));

            if (savedInstanceState != null) {
                int id = savedInstanceState.getInt(STATE_TOOL_ID, R.id.action_pencil);
                setToolFromId(id);
                if (isNarrow)
                    setButtonIconFromId(id);
                else
                    setColorFromId(id);
            }
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onColorChanged(int newColor) {

    }

    @Override
    public void onPreEdit() {

    }

    @Override
    public void onEdit() {
        frameAdapter.invalidateFrame(editorView.getEditingFrame());
    }
}
