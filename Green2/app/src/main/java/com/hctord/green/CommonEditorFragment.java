package com.hctord.green;

import android.app.ActivityManager;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.Toolbar;

import com.hctord.green.document.DocumentManager;
import com.hctord.green.document.history.HistoryManager;
import com.hctord.green.util.Utils;
import com.hctord.green.widget.PixelEditorView2;

/**
 * Contains common functionality between the two editor layouts.
 */
public abstract class CommonEditorFragment
        extends Fragment
        implements
            EditorActivity.Callbacks,
            PixelEditorView2.OnEditListener {

    public static final String ARG_HANDLE = "com.hctord.green.CommonEditorFragment.ARG_HANDLE",
                               ARG_TASK_DESCRIPTION = "com.hctord.green.CommonEditorFragment.ARG_TASK_DESCRIPTION";

    protected static final String STATE_TOOL_ID = "com.hctord.green.editor.STATE_TOOL_ID";

    private final int layoutResId;

    private int toolId = R.id.action_pencil;
    private PixelEditorView2 editorView;
    private String token;
    private DocumentManager documentManager;
    private HistoryManager historyManager;
    private ActivityManager.TaskDescription taskDescription;
    private Toolbar toolbar;

    protected CommonEditorFragment(int layoutResId) {
        this.layoutResId = layoutResId;
        token = Long.toString(System.currentTimeMillis());
        documentManager = DocumentManager.getDocumentManager();
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

    private void updateToolbarColor() {
        int color = Utils.maxSaturation(getAverageColor());
        toolbar.setBackgroundColor(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(Utils.multiplyValue(color, 0.75f));
        }
    }

    public void setToolbar(Toolbar toolbar) {
        this.toolbar = toolbar;
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(layoutResId, parent, false);
        editorView = (PixelEditorView2)view.findViewById(R.id.editor);

        try {
            Bundle args = getArguments();
            DocumentManager.OpenPixelArtInfo handle = args.getParcelable(ARG_HANDLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                taskDescription = args.getParcelable(ARG_TASK_DESCRIPTION);
            editorView.setOnEditListener(this);
            editorView.setTarget(DocumentManager.getDocumentManager().getDocument(handle));

            if (savedInstanceState != null) {
                int id = savedInstanceState.getInt(STATE_TOOL_ID, R.id.action_pencil);
                setToolFromId(id);
            }

            init(view, savedInstanceState);

            updateToolbarColor();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public int getAverageColor() {
        return editorView.getTarget().getAverageColor();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_TOOL_ID, toolId);
    }

    @Override
    public void onPreEdit() {
        if (historyManager == null) {
            historyManager = new HistoryManager(editorView.getTarget());
        }

        historyManager.saveToHistory(editorView.getEditingFrame());
    }

    @Override
    public void onEdit() {
        updateToolbarColor();
    }

    @Override
    public void clearCanvas() {
    }

    @Override
    public void centerCanvas() {
        editorView.center();
    }

    @Override
    public void saveFile(DocumentManager.OpenPixelArtInfo info, String filename) {
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

        info.setFilename(filename);
        documentManager.updateOpenInfo(info);
        saveFile(info);
    }

    @Override
    public void saveFile(DocumentManager.OpenPixelArtInfo info) {
        documentManager.pushChanges(info, editorView.getTarget());
        documentManager.saveDocument(info);
    }

    @Override
    public void onMenuInflated(Menu menu) { }
}
