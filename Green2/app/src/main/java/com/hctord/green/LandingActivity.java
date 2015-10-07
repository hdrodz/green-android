package com.hctord.green;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.hctord.green.adapters.PixelArtRecyclerAdapter;
import com.hctord.green.document.DocumentManager;
import com.hctord.green.document.FileScanner;
import com.hctord.green.util.Action;


public class LandingActivity
        extends ActionBarActivity
        implements
            PixelArtRecyclerAdapter.Callbacks {

    private DocumentManager documentManager;
    private Action<Void> onFinishCallback = new Action<Void>() {
        @Override
        public void run(Void unused) {
        }
    };
    //private PixelArtAdapter adapter;

    private PixelArtRecyclerAdapter adapter;

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enable content transitions if available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            supportRequestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_landing);

        documentManager = DocumentManager.getDocumentManager(getApplicationContext());

        int columnCount = getResources().getInteger(R.integer.column_count);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.grid);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, columnCount);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new PixelArtRecyclerAdapter(this, null, onFinishCallback, this);
        recyclerView.setAdapter(adapter);
        /*
        GridView gridView = (GridView)findViewById(R.id.grid);

        adapter = new PixelArtAdapter(this, null, onFinishCallback, this);
        gridView.setAdapter(adapter);
        */

        setupToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (documentManager.isDirty()) {
            adapter.refreshFilesList(this);
            documentManager.clearDirty();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_landing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent intent;

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_new:
                intent = new Intent(this, EditorActivity.class);
                intent.putExtra(CommonEditorFragment.ARG_HANDLE, documentManager.createDocument(16, 16));
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestShare(int position) {

    }

    @Override
    public void onRequestRename(int position) {

    }

    @Override
    public void onRequestExport(int position) {

    }

    @Override
    public void onRequestDelete(int position) {

    }

    @Override
    public void onRequestPreview(int position, View imageView) {
        Intent intent = new Intent(this, ImagePreviewActivity.class);
        Bundle options;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this, imageView, "imageView"
            );
            options = activityOptionsCompat.toBundle();
        }
        else {
            options = new Bundle();
        }

        // TODO: add parameters to options
        FileScanner.PixelArtHandle handle = adapter.getItem(position);
        intent.putExtra(ImagePreviewActivity.EXTRA_HANDLE, handle);

        startActivity(intent, options);
    }

    @Override
    public void onRequestOpen(int position) {
        FileScanner.PixelArtHandle handle = adapter.getItem(position);
        DocumentManager.OpenPixelArtInfo info = documentManager.openDocument(handle.getFilename());
        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra(CommonEditorFragment.ARG_HANDLE, info);
        startActivity(intent);
    }
}
