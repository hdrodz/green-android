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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hctord.green.document.DocumentManager;
import com.hctord.green.util.Utils;


public class EditorActivity extends AppCompatActivity {
    public static final String EXTRA_LOAD_EXISTING = "com.hctord.green.EditorActivity.loadExistingFile",
                               EXTRA_SAVED_HANDLE = "com.hctord.green.EditorActivity.savedFileHandle";

    private static final String TAG_FRAGMENT = "com.hctord.green.EditorActivity.fragment";

    private static boolean wasInLargeMode;

    private Callbacks callbacks;
    private static Bitmap ICON = null;
    private String filename;
    private DocumentManager.OpenPixelArtInfo handle;
    private TextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Checks whether the large or small editor UI will be used.
        boolean isInLargeMode = getResources().getBoolean(R.bool.use_large_ui);
        // Get the intent that is associated with the launching of this activity.
        Intent intent = getIntent();
        // Get the handle that points to the currently open document.
        handle = intent.getParcelableExtra(CommonEditorFragment.ARG_HANDLE);
        filename = handle.getFilename();
        // Title string
        String title = filename.replace(".green", "");

        setTitle(title);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        titleTextView = ((TextView)toolbar.findViewById(R.id.title));
        titleTextView.setText(title);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            CommonEditorFragment fragment =
                    isInLargeMode
                            ? new LargeEditorFragment()
                            : new SmallEditorFragment();
            fragment.setArguments(intent.getExtras());
            fragment.setToolbar(toolbar);
            callbacks = fragment;
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment, TAG_FRAGMENT)
                    .commit();
        }
        else {
            if (isInLargeMode != wasInLargeMode) {
                CommonEditorFragment fragment =
                        isInLargeMode
                                ? new LargeEditorFragment()
                                : new SmallEditorFragment();
                callbacks = fragment;
                fragment.setArguments(intent.getExtras());
                fragment.setToolbar(toolbar);
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment, TAG_FRAGMENT)
                        .commit();
            }
            else {
                CommonEditorFragment fragment =
                        (CommonEditorFragment)getFragmentManager().findFragmentByTag(TAG_FRAGMENT);
                callbacks = fragment;
                fragment.setToolbar(toolbar);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ICON == null)
                ICON = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

            int color;
            if (callbacks != null)
                color = handle.getAverageColorSat();
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                color = getResources().getColor(R.color.primary, null);
            else
                color = getResources().getColor(R.color.primary);
            setTaskDescription(new ActivityManager.TaskDescription(title, ICON, color));
        }

        wasInLargeMode = isInLargeMode;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String title = titleTextView.getText().toString();
            int color;
            if (callbacks != null)
                color = Utils.maxSaturation(callbacks.getAverageColor());
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                color = getResources().getColor(R.color.primary, null);
            else
                color = getResources().getColor(R.color.primary);
            setTaskDescription(new ActivityManager.TaskDescription(
                    title, ICON, color
            ));
        }
    }

    @Override
    protected void onDestroy() {
        // Close the associated document
        DocumentManager.getDocumentManager().closeDocument(handle);
        super.onDestroy();
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
                                titleTextView.setText(txtView.getText().toString());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    int color;
                                    if (callbacks != null)
                                            color = Utils.maxSaturation(callbacks.getAverageColor());
                                    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                        color = getResources().getColor(R.color.primary, null);
                                    else
                                        color = getResources().getColor(R.color.primary);
                                    setTaskDescription(new ActivityManager.TaskDescription(filename.replace(".green", ""), ICON, color));
                                }

                                callbacks.saveFile(handle, filename);
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
                callbacks.saveFile(handle);
            }
            return true;
        }
        else if (id == R.id.action_fullscreen) {
            if (callbacks != null)
                callbacks.toggleFullscreen(item);
        }

        return super.onOptionsItemSelected(item);
    }

    public interface Callbacks {
        void clearCanvas();
        void centerCanvas();
        void saveFile(DocumentManager.OpenPixelArtInfo info);
        void saveFile(DocumentManager.OpenPixelArtInfo info, String filename);
        void onMenuInflated(Menu menu);
        void toggleFullscreen(MenuItem fullScreenItem);
        int getAverageColor();
    }
}
