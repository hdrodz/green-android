package com.hctord.green;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hctord.green.document.DocumentManager;
import com.hctord.green.widget.PixelEditorView2;
import com.hctord.green.document.PixelArt;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Contains common functionality between the two editor layouts.
 */
public abstract class CommonEditorFragment
        extends Fragment
        implements
            EditorActivity.Callbacks,
            PixelEditorView2.OnEditListener {

    public static final String ARG_HANDLE = "com.hctord.green.CommonEditorFragment.ARG_HANDLE";

    protected static final String STATE_TOOL_ID = "com.hctord.green.editor.STATE_TOOL_ID";

    private final int layoutResId;

    private int toolId = R.id.action_pencil;
    private PixelEditorView2 editorView;
    private String token;

    protected CommonEditorFragment(int layoutResId) {
        this.layoutResId = layoutResId;
        token = Long.toString(System.currentTimeMillis());
    }

    protected final PixelEditorView2 getEditorView() {
        return editorView;
    }

    protected final void setToolFromId(int id) {
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

    protected abstract void init(View root, Bundle savedInstanceState);

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(layoutResId, parent, false);
        editorView = (PixelEditorView2)view.findViewById(R.id.editor);

        try {
            Bundle args = getArguments();
            DocumentManager.OpenPixelArtInfo handle = args.getParcelable(ARG_HANDLE);
            editorView.setTarget(DocumentManager.getDocumentManager().getDocument(handle));

            if (savedInstanceState != null) {
                int id = savedInstanceState.getInt(STATE_TOOL_ID, R.id.action_pencil);
                setToolFromId(id);
            }

            init(view, savedInstanceState);
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_TOOL_ID, toolId);
    }

    @Override
    public void clearCanvas() {
    }

    @Override
    public void centerCanvas() {
        editorView.center();
    }

    @Override
    public void saveFile(String filename) {
        /*
        try {
            FileOutputStream fos = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
            editorView.getTarget().write(fos);
            fos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(getActivity(), "Image saved", Toast.LENGTH_SHORT).show();*/
        // *snip*


    }

    @Override
    public void saveFile() {

    }

    @Override
    public void onMenuInflated(Menu menu) { }
}
