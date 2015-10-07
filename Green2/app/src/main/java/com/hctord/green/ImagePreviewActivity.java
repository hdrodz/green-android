package com.hctord.green;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.ImageView;

import com.hctord.green.document.FileScanner;


/**
 */
public class ImagePreviewActivity extends ActionBarActivity {
    public static final String EXTRA_HANDLE = "ImagePreviewActivity.handle",
                               EXTRA_FILENAME = "ImagePreviewActivity.filename",
                               EXTRA_PREVIEW = "ImagePreviewActivity.preview";

    private void init(Bundle savedInstanceState) {
        Intent intent = getIntent();

        ImageView view = (ImageView)findViewById(R.id.preview);
        FileScanner.PixelArtHandle handle = intent.getParcelableExtra(EXTRA_HANDLE);

        /*byte[] imgData = intent.getByteArrayExtra(EXTRA_PREVIEW);

        Bitmap preview = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
        BitmapDrawable bd = new BitmapDrawable(getResources(), preview);
        bd.setAntiAlias(false);
        bd.setDither(false);
        bd.setFilterBitmap(false);*/

        view.setImageDrawable(handle.getPreviewAsDrawable(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_preview);
        setupActionBar();

        init(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
