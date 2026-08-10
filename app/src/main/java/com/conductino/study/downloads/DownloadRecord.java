package com.conductino.study.downloads;

/** One entry in the internal downloads store. */
public final class DownloadRecord {
    public final long id;
    public final String url;
    public final String fileName;
    public final String localPath;  // under app filesDir/downloads/
    public final long bytes;
    public final long createdAt;
    public final String mimeType;

    public DownloadRecord(long id, String url, String fileName, String localPath,
                          long bytes, long createdAt, String mimeType) {
        this.id = id;
        this.url = url;
        this.fileName = fileName;
        this.localPath = localPath;
        this.bytes = bytes;
        this.createdAt = createdAt;
        this.mimeType = mimeType != null ? mimeType : "application/octet-stream";
    }
}
