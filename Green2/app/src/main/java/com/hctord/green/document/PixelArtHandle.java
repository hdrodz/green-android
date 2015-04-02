package com.hctord.green.document;

import android.graphics.Point;

import java.io.File;

/**
 * Contains overview information of an entry in the internal pixel art database.
 */
@Deprecated
public class PixelArtHandle {
    private String filename;
    private String thumbnailFilename;
    private Point dimensions;
    private long size;

    public PixelArtHandle(String filename, String thumbnailFilename, Point dimensions, long size) {
        this.filename = filename;
        this.thumbnailFilename = thumbnailFilename;
        this.dimensions = dimensions;
        this.size = size;
    }

    public String getFilename() {
        return filename;
    }

    public String getThumbnailFilename() {
        return thumbnailFilename;
    }

    public Point getDimensions() {
        return dimensions;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void rename(String filename) {
        File file = new File(this.filename);
        if (file.renameTo(new File(filename))) {
            this.filename = filename;
            file = new File(thumbnailFilename);
            thumbnailFilename = filename.replace(".green", "_thumbnail.png");
            file.renameTo(new File(thumbnailFilename));
        }
    }
}
