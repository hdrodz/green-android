package com.hctord.green;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hctord.green.document.DocumentManager;


public class EditorActivity extends ActionBarActivity {
    public static final String EXTRA_LOAD_EXISTING = "com.hctord.green.EditorActivity.loadExistingFile",
                               EXTRA_SAVED_HANDLE = "com.hctord.green.EditorActivity.savedFileHandle";

    private static boolean wasInLargeMode;

    private Callbacks callbacks;
    private static Bitmap ICON = null;
    private String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Checks whether the large or small editor UI will be used.
        boolean isInLargeMode = getResources().getBoolean(R.bool.use_large_ui);
        // Get the intent that is associated with the launching of this activity.
        Intent intent = getIntent();
        // Get the handle that points to the currently open document.
        DocumentManager.OpenPixelArtInfo handle = intent.getParcelableExtra(CommonEditorFragment.ARG_HANDLE);
        filename = handle.getFilename();
        // Title string
        String title = filename.replace(".green", "");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ((TextView)toolbar.findViewById(R.id.title)).setText(title);
        setSupportActionBar(toolbar);

        setTitle(title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ICON == null)
                ICON = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
            int color = getResources().getColor(R.color.primary);
            setTaskDescription(new ActivityManager.TaskDescription(title, ICON, color));
        }

        if (savedInstanceState == null) {
            CommonEditorFragment fragment =
                    isInLargeMode
                            ? new LargeEditorFragment()
                            : new EditorFragment();
            fragment.setArguments(intent.getExtras());
            callbacks = fragment;
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
        else {
            if (isInLargeMode != wasInLargeMode) {
                CommonEditorFragment fragment =
                        isInLargeMode
                                ? new LargeEditorFragment()
                                : new EditorFragment();
                callbacks = fragment;
                fragment.setArguments(intent.getExtras());
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit();
            }
        }
        wasInLargeMode = isInLargeMode;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        if (callbacks != null)
            callbacks.onMenuInflated(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_save) {
            if (filename == null || !filename.endsWith(".green")) {
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_save, null);
                final EditText txtView = (EditText) dialogView.findViewById(R.id.text);
                Dialog saveDialog = new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setTitle(R.string.title_activity_save_image)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                filename = txtView.getText().toString() + ".green";
                                setTitle(txtView.getText().toString());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    int color = getResources().getColor(R.color.primary);
                                    setTaskDescription(new ActivityManager.TaskDescription(filename.replace(".green", ""), ICON, color));
                                }
                                callbacks.saveFile(filename);
                                dialog.dismiss();
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
                callbacks.saveFile(filename);
            }
            return true;
        }
        else if (id == R.id.action_fullscreen) {
            if (callbacks != null)
                callbacks.toggleFullscreen(item);
        }

        return super.onOptionsItemSelected(item);
    }

    public static interface Callbacks {
        public void clearCanvas();
        public void centerCanvas();
        public void saveFile();
        public void saveFile(String filename);
        public void onMenuInflated(Menu menu);
        public void toggleFullscreen(MenuItem fullScreenItem);
    }
}
