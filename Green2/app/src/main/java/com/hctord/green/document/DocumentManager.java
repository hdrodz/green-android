package com.hctord.green.document;

import android.content.Context;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hctord.green.adapters.OpenDocumentAdapter;
import com.hctord.green.util.Utils;

public class DocumentManager {

    private static DocumentManager singleton = null;

    public static void initialize(Context context) {
        singleton = new DocumentManager(context);
    }

    public static DocumentManager getDocumentManager() {
        return singleton;
    }

    public static DocumentManager getDocumentManager(Context context) {
        if (singleton == null)
            initialize(context);
        return singleton;
    }

    private List<OpenPixelArtInfo> openDocumentInfoList;
    private List<PixelArt> openDocuments;
    private Context context;
    private ImageRenderer thumbRenderer;
    private int newDocuments = 0;
    private OpenDocumentAdapter openDocumentAdapter;
    private boolean isDirty = false;

    private DocumentManager(Context context) {
        this.context = context;
        thumbRenderer = new ImageRenderer();
        openDocumentInfoList = new ArrayList<>();
        openDocuments = new ArrayList<>();
        openDocumentAdapter = new OpenDocumentAdapter(openDocumentInfoList);
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void clearDirty() {
        isDirty = false;
    }

    public OpenPixelArtInfo createDocument(int w, int h) {
        // Create and populate new handle entry
        OpenPixelArtInfo handle = new OpenPixelArtInfo();
        handle.filename = newDocuments > 0 ? String.format("Untitled %d", newDocuments) : "Untitled";
        PixelArt pixelArt = new PixelArt(w, h);
        openDocuments.add(pixelArt);
        handle.openDocumentIndex = openDocuments.indexOf(pixelArt);
        handle.artHashCode = pixelArt.hashCode();
        handle.newFile = true;
        openDocumentInfoList.add(handle);
        ++newDocuments;
        openDocumentAdapter.documentAdded();
        return handle;
    }

    public void closeDocument(OpenPixelArtInfo info) {
        closeDocument(info, false);
    }

    public void closeDocument(OpenPixelArtInfo info, boolean save) {
        int index = openDocumentInfoList.indexOf(info);
        openDocumentInfoList.remove(info);
        if (save) {
            saveDocument(info);
        }
        openDocuments.remove(info.openDocumentIndex);
        openDocumentAdapter.documentClosed(index);
    }

    public OpenPixelArtInfo openDocument(String filename) {
        OpenPixelArtInfo info = new OpenPixelArtInfo();
        info.filename = filename.replace(".green", "");
        PixelArt art;
        try {
            FileInputStream fis = context.openFileInput(filename);
            art = new PixelArt(fis);
            fis.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        openDocuments.add(art);
        info.openDocumentIndex = openDocuments.indexOf(art);
        openDocumentInfoList.add(info);
        openDocumentAdapter.documentAdded();
        return info;
    }

    public void saveDocument(OpenPixelArtInfo info) {
        PixelArt art = openDocuments.get(info.openDocumentIndex);
        info.newFile = false;
        try {
            FileOutputStream fos = context.openFileOutput(info.filename + ".green", Context.MODE_PRIVATE);
            art.write(fos);
            fos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        info.artHashCode = art.hashCode();
        isDirty = true;
    }

    public void updateOpenInfo(OpenPixelArtInfo newInfo) {
        OpenPixelArtInfo oldInfo = null;
        for (OpenPixelArtInfo info : openDocumentInfoList) {
            if (info.getOpenDocumentIndex() == newInfo.getOpenDocumentIndex()) {
                oldInfo = info;
                break;
            }
        }

        if (oldInfo == null)
            throw new IllegalArgumentException("No matching info");

        oldInfo.filename = newInfo.filename;
    }

    public void pushChanges(OpenPixelArtInfo info, PixelArt changes) {
        openDocuments.set(info.openDocumentIndex, changes);
    }

    public PixelArt getDocument(OpenPixelArtInfo info) {
        PixelArt output = openDocuments.get(info.openDocumentIndex);
        /*
        if (output.hashCode() != info.artHashCode) {
            // Hash code mismatch, reload the file.
            try {
                FileInputStream fis = context.openFileInput(info.filename);
                output = new PixelArt(fis);
                fis.close();
            }
            catch (IOException e) {
                return null;
            }
            openDocuments.add(output);
            info.artHashCode = output.hashCode();
            info.openDocumentIndex = openDocuments.indexOf(output);
        }*/
        return output;
    }

    public void autosaveAll() {
        for (OpenPixelArtInfo info : openDocumentInfoList) {
            PixelArt doc = openDocuments.get(info.openDocumentIndex);
            String filename = "$" + info.filename;
            try {
                FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
                doc.write(fos);
                fos.close();
            }
            catch (IOException e) {
                Log.e("Autosave", "Failed to write file " + info.filename + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public List<OpenPixelArtInfo> getOpenDocumentInfoList() {
        return openDocumentInfoList;
    }

    public Context getContext() {
        return context;
    }

    /**
     *
     */
    public static class OpenPixelArtInfo implements Parcelable {
        private int openDocumentIndex;
        private String filename;
        private boolean newFile = true;
        private int artHashCode;
        private int averageColorSat;

        private OpenPixelArtInfo() {}

        private OpenPixelArtInfo(Parcel in) {
            openDocumentIndex = in.readInt();
            filename = in.readString();
            newFile = in.readByte() != 0;
        }

        public int getOpenDocumentIndex() {
            return openDocumentIndex;
        }

        public String getFilename() {
            return filename;
        }

        public boolean isNewFile() {
            return newFile;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(openDocumentIndex);
            out.writeString(filename);
            out.writeByte((byte)(isNewFile() ? 1 : 0));
        }

        public static final Creator<OpenPixelArtInfo> CREATOR = new Creator<OpenPixelArtInfo>() {
            @Override
            public OpenPixelArtInfo createFromParcel(Parcel parcel) {
                return new OpenPixelArtInfo(parcel);
            }

            @Override
            public OpenPixelArtInfo[] newArray(int len) {
                return new OpenPixelArtInfo[len];
            }
        };

        public int getAverageColorSat() {
            return averageColorSat;
        }
    }

}
