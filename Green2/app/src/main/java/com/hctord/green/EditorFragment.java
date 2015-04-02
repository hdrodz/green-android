package com.hctord.green;

import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.PopupMenu;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.hctord.green.adapters.PaletteAdapter;
import com.hctord.green.util.ColorPickerPopupWindow;
import com.hctord.green.widget.PixelEditorView2;

/**
 * Fragment that manages the editor on a standard sized screen
 */
public class EditorFragment
        extends CommonEditorFragment
        implements
            ColorPickerPopupWindow.OnColorChangedListener {

    private static final View.OnClickListener TOOL_BUTTON_LISTENER = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            PopupMenu menu = (PopupMenu)view.getTag();
            menu.show();
        }
    };
    private final View.OnClickListener PALETTE_BUTTON_LISTENER = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (paletteWindow != null) {
                paletteWindow.showAsDropDown(view, paletteOffsetX, paletteOffsetY);
            }
        }
    };
    private final View.OnClickListener ADD_COLOR_BUTTON_LISTENER = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Set the modifying color to an invalid index so that we know that it is going to add
            // a new color when 'ok' is pressed.
            modifyingColor = -1;
            paletteWindow.dismiss();
            colorPopupWindow.showAsDropDown(paletteButton, paletteOffsetX, paletteOffsetY);
        }
    };
    private final PopupWindow.OnDismissListener REOPEN_PALETTE_ON_DISMISS = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            paletteWindow.showAsDropDown(paletteButton, paletteOffsetX, paletteOffsetY);
        }
    };

    private int modifyingColor;
    private int paletteOffsetY, paletteOffsetX;
    private PopupMenu toolboxMenu;
    private ImageButton toolboxButton, paletteButton;
    private PixelEditorView2 editorView;
    private PopupWindow paletteWindow;
    private ColorPickerPopupWindow colorPopupWindow;
    private PaletteAdapter paletteAdapter;

    private PopupMenu.OnMenuItemClickListener toolboxListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            setButtonIconFromId(menuItem.getItemId());
            setToolFromId(menuItem.getItemId());
            return true;
        }
    };

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

    public EditorFragment() {
        super(R.layout.fragment_editor);
    }

    @Override
    protected void init(View root, Bundle savedInstanceState) {
        Resources res = getResources();
        paletteOffsetX = (int)res.getDimension(R.dimen.palette_popup_offset_x);
        paletteOffsetY = (int)res.getDimension(R.dimen.palette_popup_offset_y);
        editorView = super.getEditorView();

        // Set up the toolbox button
        toolboxButton = (ImageButton)root.findViewById(R.id.tools_button);
        toolboxMenu = new PopupMenu(getActivity(), toolboxButton);
        toolboxMenu.inflate(R.menu.context_tools);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            toolboxButton.setOnTouchListener(toolboxMenu.getDragToOpenListener());
        }
        toolboxButton.setTag(toolboxMenu);
        toolboxButton.setOnClickListener(TOOL_BUTTON_LISTENER);
        toolboxMenu.setOnMenuItemClickListener(toolboxListener);

        // Set up the palette button
        paletteButton = (ImageButton)root.findViewById(R.id.palette_button);
        paletteButton.setOnClickListener(PALETTE_BUTTON_LISTENER);

        View paletteWindowRoot = LayoutInflater.from(getActivity()).inflate(
                R.layout.popup_palette, null, false
        );

        // Palette window

        paletteWindow = new PopupWindow();
        paletteWindow.setContentView(paletteWindowRoot);
        paletteWindow.setWidth((int) res.getDimension(R.dimen.popup_palette_width));
        paletteWindow.setHeight((int) res.getDimension(R.dimen.popup_palette_height));
        paletteWindow.setFocusable(true);
        paletteWindow.setBackgroundDrawable(res.getDrawable(android.R.drawable.dialog_holo_light_frame));

        // Color picker window

        colorPopupWindow = new ColorPickerPopupWindow(getActivity(), (ViewGroup)paletteWindowRoot);
        colorPopupWindow.setOnColorChangedListener(this);
        colorPopupWindow.setOnDismissListener(REOPEN_PALETTE_ON_DISMISS);

        ListView paletteList = (ListView)paletteWindowRoot.findViewById(R.id.palette_list);
        paletteAdapter = new PaletteAdapter(getActivity(), editorView.getTarget().getPalette());
        paletteList.setAdapter(paletteAdapter);
        paletteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editorView.setBrushColor(position);
                paletteButton.setColorFilter(paletteAdapter.getItem(position));
            }
        });
        paletteList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                modifyingColor = position;
                paletteWindow.dismiss();
                colorPopupWindow.setColor(paletteAdapter.getItem(position));
                colorPopupWindow.showAsDropDown(paletteButton, paletteOffsetX, paletteOffsetY);
                return true;
            }
        });

        ImageButton addButton = (ImageButton)paletteWindowRoot.findViewById(R.id.add_color);
        addButton.setOnClickListener(ADD_COLOR_BUTTON_LISTENER);

        if (savedInstanceState != null) {
            int toolId = savedInstanceState.getInt(STATE_TOOL_ID, R.id.action_pencil);
            setButtonIconFromId(toolId);
        }
    }

    @Override
    public void onPreEdit() {

    }

    @Override
    public void onEdit() {

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
    public void toggleFullscreen(MenuItem item) {}
}
