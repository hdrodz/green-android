package com.hctord.green;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.hctord.green.adapters.FrameRecyclerAdapter;
import com.hctord.green.adapters.PaletteRecyclerAdapter;
import com.hctord.green.document.DocumentManager;
import com.hctord.green.util.ColorPickerPopupWindow;
import com.hctord.green.util.Utils;
import com.hctord.green.widget.PixelEditorView2;

import static android.view.View.GONE;

public class NuEditorActivity
        extends AppCompatActivity
        implements ColorPickerPopupWindow.OnColorChangedListener,
                   PixelEditorView2.OnEditListener,
                   PaletteRecyclerAdapter.Callbacks,
                   FrameRecyclerAdapter.Callbacks {

    public static final String EXTRA_HANDLE = "com.hctord.green.NuEditorActivity.EXTRA_HANDLE";

    public static final String STATE_TOOL_ID = "com.hctord.green.NuEditorActivity.STATE_TOOL_ID";
    public static final String STATE_PALETTE_INDEX = "com.hctord.green.NuEditorActivity.STATE_PALETTE_INDEX";
    public static final String STATE_FRAME_INDEX = "com.hctord.green.NuEditorActivity.STATE_FRAME_INDEX";
    public static final String STATE_FILENAME = "com.hctrod.green.NuEditorActivity.STATE_FILENAME";

    private static final View.OnClickListener TOOL_BUTTON_LISTENER = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu menu = (PopupMenu)v.getTag();
            menu.show();
        }
    };

    private static Bitmap ICON = null;

    // UI Elements
    private PixelEditorView2 editorView;
    private PaletteRecyclerAdapter paletteAdapter;
    private FrameRecyclerAdapter framesAdapter;
    private RecyclerView paletteList;
    private RecyclerView framesList;
    private ImageButton toolboxButton,
                        paletteButton,
                        framesButton;
    private ImageButton lastToolButton;
    private View framesPanel,
                 palettePanel,
                 toolboxPanel;
    private View shade;
    private Toolbar toolbar;
    private ColorPickerPopupWindow colorPopupWindow;

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
    private Animation framesFlyOutAnimation;
    private Animation framesFlyInAnimation;
    private Animation paletteFlyOutAnimation;
    private Animation paletteFlyInAnimation;
    private Animation toolboxFlyOutAnimation;
    private Animation toolboxFlyInAnimation;

    // State
    private String filename = null;
    private boolean isNarrow = false;
    private boolean isPaletteVisible = false,
                    isFramesVisible = false;
    private boolean isFullscreen;
    private int toolId;
    private int colorIndex;
    private int frameIndex;
    private int modifyingColor;
    private ActivityManager.TaskDescription taskDescription;
    private DocumentManager documentManager;
    private DocumentManager.OpenPixelArtInfo handle;

    ////////////////////////////////////////////
    //
    // Misc utility functions
    //
    ///////////////////////////////////////////

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

    private int getAverageColor() {
        return editorView.getTarget().getAverageColor(editorView.getEditingFrame());
    }

    private void setThemeColor() {
        int color = Utils.maxSaturation(getAverageColor());
        toolbar.setBackgroundColor(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Utils.multiplyValue(color, 0.75f));
            setTaskDescription(new ActivityManager.TaskDescription(
                    filename != null ? filename : getResources().getString(R.string.app_name),
                    ICON, color));
        }
    }

    private void save() {
        if (filename == null || handle.isNewFile()) {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_save, null);
            final EditText txtView = (EditText) dialogView.findViewById(R.id.text);
            Dialog saveDialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setTitle(R.string.title_activity_save_image)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            filename = txtView.getText().toString();
                            setThemeColor();
                            handle.setFilename(filename);
                            NuEditorActivity.this.setTitle(filename);
                            documentManager.pushChanges(handle, editorView.getTarget());
                            documentManager.saveDocument(handle);
                            dialog.dismiss();
                            Toast.makeText(NuEditorActivity.this, R.string.message_saved, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .create();
            saveDialog.show();
        }
        else {
            documentManager.pushChanges(handle, editorView.getTarget());
            documentManager.saveDocument(handle);
            Toast.makeText(this, R.string.message_saved, Toast.LENGTH_SHORT).show();
        }
    }

    ////////////////////////////////////////////
    //
    // Loading utility functions
    //
    ///////////////////////////////////////////

    private void loadAnimations() {
        // UI mode-independent
        framesFlyInAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        framesFlyOutAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
        framesFlyOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }
            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                framesPanel.setVisibility(GONE);
                isFramesVisible = false;
            }
        });

        // UI mode-dependent
        if (isNarrow) {
            paletteFlyInAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
            paletteFlyOutAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
            paletteFlyOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }
                @Override
                public void onAnimationRepeat(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    palettePanel.setVisibility(GONE);
                    isPaletteVisible = false;
                }
            });
        }
        else {

        }
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

        // Setup action bar
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && ICON == null) {
            ICON = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_nobg);
        }

        // Load the current image into the editor
        documentManager = DocumentManager.getDocumentManager(getApplicationContext());
        handle = getIntent().getParcelableExtra(EXTRA_HANDLE);
        editorView = (PixelEditorView2)findViewById(R.id.editor);
        editorView.setTarget(documentManager.getDocument(handle));

        // Load state
        if (savedInstanceState != null) {
            filename = savedInstanceState.getString(STATE_FILENAME);
            colorIndex = savedInstanceState.getInt(STATE_PALETTE_INDEX, 1);
            frameIndex = savedInstanceState.getInt(STATE_FRAME_INDEX, 0);
        }
        else {
            filename = handle.getFilename();
            colorIndex = 1;
            frameIndex = 0;
        }

        if (filename != null)
            setTitle(filename);

        setThemeColor();

        // Test if the button bar exists in this view.
        isNarrow = findViewById(R.id.button_bar) != null;

        // Get the panels of the UI for animations
        framesPanel = findViewById(R.id.frames_panel);
        toolboxPanel = findViewById(R.id.toolbox_panel);
        palettePanel = findViewById(R.id.palette_panel);

        // Setup palette list
        paletteList = (RecyclerView)findViewById(R.id.palette_list);
        paletteAdapter = new PaletteRecyclerAdapter(editorView.getTarget().getPalette());
        paletteAdapter.setListener(this);
        paletteList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        paletteList.setAdapter(paletteAdapter);
        onSelectColor(colorIndex);

        colorPopupWindow = new ColorPickerPopupWindow(this, (ViewGroup)findViewById(android.R.id.content));
        colorPopupWindow.setOnColorChangedListener(this);

        // Setup frame list
        framesList = (RecyclerView)findViewById(R.id.frames_list);
        framesAdapter = new FrameRecyclerAdapter(this, editorView.getTarget());
        framesAdapter.setListener(this);
        framesList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        framesList.setAdapter(framesAdapter);
        onSelectFrame(frameIndex);

        ImageButton addColorButton = (ImageButton)findViewById(R.id.add_color);
        addColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyingColor = -1;
                colorPopupWindow.showAsDropDown(v);
            }
        });
        ImageButton addFramesButton = (ImageButton)findViewById(R.id.add_frame);
        addFramesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorView.addFrame();
                framesAdapter.invalidateFrames();
                framesAdapter.notifyDataSetChanged();
            }
        });

        loadAnimations();

        if (isNarrow) { // Phone or portrait view

            // Get buttons
            toolboxButton = (ImageButton)findViewById(R.id.tools_button);
            paletteButton = (ImageButton)findViewById(R.id.palette_button);
            framesButton = (ImageButton)findViewById(R.id.layers_button);

            paletteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isPaletteVisible) {
                        palettePanel.setVisibility(View.VISIBLE);
                        palettePanel.startAnimation(paletteFlyInAnimation);
                        isPaletteVisible = true;
                    }
                }
            });

            framesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isFramesVisible) {
                        framesPanel.setVisibility(View.VISIBLE);
                        framesPanel.startAnimation(framesFlyInAnimation);
                        isFramesVisible = true;
                    }
                }
            });

            PopupMenu toolboxMenu = new PopupMenu(this, toolboxButton);
            toolboxMenu.inflate(R.menu.context_tools);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                toolboxButton.setOnTouchListener(toolboxMenu.getDragToOpenListener());
            }
            toolboxButton.setTag(toolboxMenu);
            toolboxButton.setOnClickListener(TOOL_BUTTON_LISTENER);
            toolboxMenu.setOnMenuItemClickListener(toolboxListener);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_TOOL_ID, toolId);
        outState.putInt(STATE_FRAME_INDEX, frameIndex);
        outState.putInt(STATE_PALETTE_INDEX, colorIndex);
        outState.putString(STATE_FILENAME, filename);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_save:
                save();
                return true;
            default:
                return false;
        }
    }

    ////////////////////////////////////////////
    //
    // OnColorChangedListener overrides
    //
    ///////////////////////////////////////////

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

    ///////////////////////////////////////////
    //
    // OnEditListener overrides
    //
    ///////////////////////////////////////////

    @Override
    public void onPreEdit() {

    }

    @Override
    public void onEdit() {
        framesAdapter.invalidateFrame(editorView.getEditingFrame());
        setThemeColor();
    }

    ///////////////////////////////////////////
    //
    // Palette adapter callbacks overrides
    //
    ///////////////////////////////////////////

    @Override
    public void onSelectColor(int position) {
        editorView.setBrushColor(position);
        colorIndex = position;
        if (isNarrow && isPaletteVisible) {
            palettePanel.startAnimation(paletteFlyOutAnimation);
        }
    }

    @Override
    public void onLongTapColor(int position, View anchor) {
        modifyingColor = position;
        colorPopupWindow.setColor(editorView.getTarget().getPalette().get(position));
        // TODO: improve offset calculation
        colorPopupWindow.showAsDropDown(anchor, 0,
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -8.0f,
                        getResources().getDisplayMetrics()));
    }

    ///////////////////////////////////////////
    //
    // Frame adapter callbacks overrides
    //
    ///////////////////////////////////////////

    @Override
    public void onSelectFrame(int position) {
        editorView.setEditingFrame(position);
        frameIndex = position;
        if (isNarrow && isFramesVisible) {
            framesPanel.startAnimation(framesFlyOutAnimation);
        }
    }
}
